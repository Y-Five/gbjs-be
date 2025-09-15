/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfive.gbjs.domain.course.dto.request.CourseRequest;
import com.yfive.gbjs.domain.course.dto.response.CourseResponse;
import com.yfive.gbjs.domain.seal.repository.SealSpotRepository;
import com.yfive.gbjs.domain.spot.service.SpotService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** AI 기반 여행 코스 생성 서비스 (JSON 모드 강제 + 단일 호출 + 후검증/보정) */
@Service
@RequiredArgsConstructor
@Slf4j
public class CourseGenerationAiService {

  private final VectorStore vectorStore;
  private final ChatClient chatClient;
  private final SpotService spotService;
  private final SealSpotRepository sealSpotRepository;
  private final ObjectMapper objectMapper;

  /** 안전한 문자열 추출 */
  private static String s(Map<String, Object> m, String k) {
    if (m == null) return null;
    Object v = m.get(k);
    return v == null ? null : v.toString();
  }

  /** 안전한 Double 추출 */
  private static Double d(Map<String, Object> m, String k) {
    if (m == null) return null;
    Object v = m.get(k);
    if (v == null) return null;
    try {
      return Double.valueOf(v.toString());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /** 코드펜스/앞뒤 잡음 제거 */
  private static String cleanJsonFences(String raw) {
    if (raw == null) return null;
    String txt = raw.trim();
    if (txt.startsWith("```")) {
      int firstNl = txt.indexOf('\n');
      if (firstNl > 0) {
        txt = txt.substring(firstNl + 1);
      }
    }
    if (txt.length() >= 3 && txt.substring(txt.length() - 3).equals("```")) {
      txt = txt.substring(0, txt.length() - 3);
    }
    return txt.trim();
  }

  /** AI 기반으로 여행 코스를 생성. */
  public CourseResponse.CourseDetailDTO generateAiCourse(
      CourseRequest.CreateCourseRequest request) {
    Objects.requireNonNull(request, "request must not be null");
    Objects.requireNonNull(request.getStartDate(), "startDate must not be null");
    Objects.requireNonNull(request.getEndDate(), "endDate must not be null");
    Objects.requireNonNull(request.getLocations(), "locations must not be null");

    // 파라미터/일수 계산
    LocalDate start = request.getStartDate();
    LocalDate end = request.getEndDate();
    int expectedDays = (int) ChronoUnit.DAYS.between(start, end) + 1; // 포함 범위
    if (expectedDays <= 0) {
      throw new IllegalArgumentException("endDate must be on/after startDate");
    }

    // 1) Qdrant에서 관광지 검색
    String query = String.join(" ", request.getLocations()) + " 여행지";
    // 동적 topK: 일수×15, 최소 50, 최대 100
    int topK = Math.max(50, Math.min(100, expectedDays * 15));

    List<Document> allRelevantDocuments = new ArrayList<>();
    SearchRequest searchRequest = SearchRequest.builder().query(query).topK(topK).build();
    allRelevantDocuments.addAll(vectorStore.similaritySearch(searchRequest));

    // 2) 문서 → SimpleSpotDTO 변환 (유효성/타입 필터 + 중복 제거)
    Map<Long, CourseResponse.SimpleSpotDTO> uniq = new LinkedHashMap<>();
    for (Document doc : allRelevantDocuments) {
      Map<String, Object> md = doc.getMetadata();
      if (md == null) continue;
      String contentIdStr = s(md, "contentId");
      if (contentIdStr == null) continue;
      Long id;
      try {
        id = Long.valueOf(contentIdStr);
      } catch (NumberFormatException e) {
        continue;
      }

      String type = s(md, "type");
      String entityType = s(md, "entity_type"); // entity_type 추가
      if (!"spot".equals(type)) continue; // 허용 타입만 (seal_spot도 type은 spot)

      if (!uniq.containsKey(id)) {
        CourseResponse.SimpleSpotDTO dto =
            CourseResponse.SimpleSpotDTO.builder()
                .spotId(id)
                .name(s(md, "name"))
                .category(s(md, "category"))
                .addr1(s(md, "addr1"))
                .latitude(d(md, "latitude"))
                .longitude(d(md, "longitude"))
                .isSealSpot("seal_spot".equals(entityType))
                .sealSpotId(
                    "seal_spot".equals(entityType)
                        ? (s(md, "sealSpotId") == null ? null : Long.valueOf(s(md, "sealSpotId")))
                        : null)
                .build();
        uniq.put(id, dto);
      }
    }
    List<CourseResponse.SimpleSpotDTO> deduped = new ArrayList<>(uniq.values());

    // 좌표/카테고리 누락 항목은 뒤로
    deduped.sort(
        (a, b) -> {
          int p1 =
              (a.getLatitude() == null || a.getLongitude() == null ? 1 : 0)
                  + (a.getCategory() == null ? 1 : 0);
          int p2 =
              (b.getLatitude() == null || b.getLongitude() == null ? 1 : 0)
                  + (b.getCategory() == null ? 1 : 0);
          return Integer.compare(p1, p2);
        });

    List<CourseResponse.SimpleSpotDTO> seal =
        deduped.stream()
            .filter(CourseResponse.SimpleSpotDTO::getIsSealSpot)
            .collect(Collectors.toList());
    List<CourseResponse.SimpleSpotDTO> general =
        deduped.stream().filter(s -> !s.getIsSealSpot()).collect(Collectors.toList());

    // 3) 간단 선정 로직 (재현 가능한 셔플 + 비율/상한)
    int maxTotal = Math.min(expectedDays * 5, 20); // 하루 5개, 전체 20개 상한
    String seedBase = start + "|" + end + "|" + String.join(",", request.getLocations());
    Random rand = new Random(seedBase.hashCode());
    java.util.Collections.shuffle(seal, rand);
    java.util.Collections.shuffle(general, rand);

    int sealTarget = (int) Math.round(maxTotal * 0.4); // 40% 목표
    sealTarget = Math.max(Math.min(sealTarget, seal.size()), Math.min(2, maxTotal)); // 최소 2 보정

    List<CourseResponse.SimpleSpotDTO> spotsForOpenAI = new ArrayList<>();
    if (sealTarget > 0 && !seal.isEmpty()) {
      spotsForOpenAI.addAll(seal.subList(0, Math.min(sealTarget, seal.size())));
    }
    int remain = maxTotal - spotsForOpenAI.size();
    if (remain > 0 && !general.isEmpty()) {
      spotsForOpenAI.addAll(general.subList(0, Math.min(remain, general.size())));
    }
    if (spotsForOpenAI.isEmpty() && !deduped.isEmpty()) {
      spotsForOpenAI.add(deduped.get(0)); // 완전 빈 경우 대비 안전장치
    }

    // 4) 프롬프트 구성 (간결/JSON 전용)
    String spotsJson;
    try {
      spotsJson = objectMapper.writeValueAsString(spotsForOpenAI);
    } catch (JsonProcessingException e) {
      log.error("OpenAI 프롬프트용 관광지 목록 JSON 변환 실패", e);
      throw new RuntimeException("AI 프롬프트 준비 실패: " + e.getMessage());
    }

    String prompt =
        """
        Return ONLY a single JSON object (no markdown, no commentary).
        It MUST match:
        {
          "title": string,
          "startDate": "YYYY-MM-DD",
          "endDate": "YYYY-MM-DD",
          "dailyCourses": [
            {
              "dayNumber": int,
              "date": "YYYY-MM-DD",
              "location": string,
              "spots": [
                {
                  "spotId": long,
                  "visitOrder": int,
                  "name": string,
                  "category": string,
                  "addr1": string,
                  "latitude": number,
                  "longitude": number,
                  "isSealSpot": boolean,
                  "sealSpotId": long|null
                }
              ]
            }
          ]
        }

        Constraints:
        - Trip length = %d days from %s to %s. dailyCourses length MUST be exactly %d (one per day).
        - Up to 5 spots per day. Use ONLY values from the provided spots list verbatim.
        - If not a seal spot: isSealSpot=false, sealSpotId=null.
        - location MUST match the dominant city/county of that day's spots.
        - If you are at risk of running out of tokens, prioritize completing ALL days; reduce per-spot fields in this order: remove addr1, then category.
        - Output compact JSON without extra whitespace.

        Available spots (JSON array):
        %s
        """
            .formatted(
                expectedDays,
                request.getStartDate(),
                request.getEndDate(),
                expectedDays,
                spotsJson);

    log.info(
        "Spots sent to OpenAI ({} total): {}",
        spotsForOpenAI.size(),
        spotsForOpenAI.stream()
            .limit(15)
            .map(s -> s.getName() + "(Seal:" + s.getIsSealSpot() + ")")
            .collect(Collectors.joining(", ")));

    // 5) 단일 호출 + JSON 모드 강제
    OpenAiChatOptions options =
        OpenAiChatOptions.builder()
            .temperature(0.2)
            .maxCompletionTokens(3072)
            .responseFormat(
                ResponseFormat.builder()
                    .type(ResponseFormat.Type.JSON_OBJECT) // 바로 이 부분입니다!
                    .build())
            .build();

    String aiResponse;
    try {
      aiResponse = chatClient.prompt().options(options).user(prompt).call().content();
    } catch (Exception e) {
      log.error("OpenAI 호출 실패", e);
      throw new RuntimeException("AI 호출 실패: " + e.getMessage());
    }

    String cleaned = cleanJsonFences(aiResponse);
    CourseResponse.CourseDetailDTO result = null;

    // 6) 1차 파싱 시도
    try {
      result = parseStrict(cleaned, objectMapper);
    } catch (Exception e) {
      log.warn("JSON 1차 파싱 실패, 복구 시도 진행: {}", e.toString());
      // 7) 복구 프롬프트
      String repairPrompt =
          """
        Return ONLY a valid JSON object for CourseResponse.CourseDetailDTO.
        Rules:
        - If any array/object tail is truncated, REMOVE the incomplete tail and close the JSON.
        - Keep existing fields/values as much as possible.
        - NO markdown, NO commentary.

        INPUT:
        %s
        """
              .formatted(cleaned);

      String repaired;
      try {
        repaired =
            chatClient
                .prompt()
                .options(options) // 동일하게 JSON 모드 유지
                .user(repairPrompt)
                .call()
                .content();
      } catch (Exception e2) {
        log.error("JSON 복구 호출 실패", e2);
        throw new RuntimeException("AI 복구 호출 실패: " + e2.getMessage());
      }

      String repairedClean = cleanJsonFences(repaired);
      try {
        result = parseStrict(repairedClean, objectMapper);
      } catch (Exception e2) {
        log.error(
            "JSON 복구 실패. 원본/복구 응답 일부: orig={}, repaired={}",
            aiResponse == null
                ? "null"
                : aiResponse.substring(0, Math.min(aiResponse.length(), 400)),
            repaired == null ? "null" : repaired.substring(0, Math.min(repaired.length(), 400)),
            e2);
        throw new RuntimeException("AI 코스 생성 실패: " + e2.getMessage());
      }
    }

    // 8) 후검증/보정: 일수 맞추기 + location 정합성 (불변 재조립)
    result = postFix(result, start, end, request.getLocations());

    // 최종 일수 확인
    if (result.getDailyCourses() == null || result.getDailyCourses().size() != expectedDays) {
      log.warn(
          "LLM returned {} days, expected {}",
          result.getDailyCourses() == null ? 0 : result.getDailyCourses().size(),
          expectedDays);
    }

    return result;
  }

  // ======= 내부 유틸 =======

  private CourseResponse.CourseDetailDTO parseStrict(String json, ObjectMapper om)
      throws Exception {
    om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    JsonNode root = om.readTree(json);
    if (root == null || !root.isObject()) {
      throw new RuntimeException("AI 응답이 유효한 JSON 오브젝트가 아님");
    }
    return om.readValue(json, CourseResponse.CourseDetailDTO.class);
  }

  private CourseResponse.CourseDetailDTO postFix(
      CourseResponse.CourseDetailDTO res,
      LocalDate start,
      LocalDate end,
      List<String> reqLocations) {

    int expectedDays = (int) ChronoUnit.DAYS.between(start, end) + 1;

    List<CourseResponse.DailyCourseDTO> inputDays =
        res.getDailyCourses() != null ? res.getDailyCourses() : List.of();

    // 날짜키로 정리
    Map<LocalDate, CourseResponse.DailyCourseDTO> byDate =
        inputDays.stream()
            .filter(dc -> dc.getDate() != null)
            .collect(
                Collectors.toMap(
                    CourseResponse.DailyCourseDTO::getDate,
                    dc -> dc,
                    (a, b) -> a,
                    LinkedHashMap::new));

    List<CourseResponse.DailyCourseDTO> fixed = new ArrayList<>();
    for (int i = 0; i < expectedDays; i++) {
      LocalDate date = start.plusDays(i);
      CourseResponse.DailyCourseDTO original = byDate.get(date);

      String location;
      List<CourseResponse.SimpleSpotDTO> spots;

      if (original == null) {
        location = guessLocationFromReq(reqLocations, i);
        spots = List.of();
      } else {
        spots = original.getSpots() != null ? original.getSpots() : List.of();
        // visitOrder 정렬/보정(불변 복제)
        spots = normalizeVisitOrderSimple(spots);

        String inferred = guessLocationFromSpotsOrReq(spots, reqLocations, i);
        if (original.getLocation() == null || original.getLocation().isBlank()) {
          location = inferred;
        } else {
          location = (inferred == null || inferred.isBlank()) ? original.getLocation() : inferred;
        }
      }

      CourseResponse.DailyCourseDTO day =
          CourseResponse.DailyCourseDTO.builder()
              .dayNumber(i + 1)
              .date(date)
              .location(location)
              .spots(spots)
              .build();

      fixed.add(day);
    }

    String title =
        (res.getTitle() == null || res.getTitle().isBlank())
            ? expectedDays
                + "-Day Trip: "
                + ((reqLocations == null || reqLocations.isEmpty())
                    ? "여행"
                    : String.join(", ", reqLocations))
            : res.getTitle();

    return CourseResponse.CourseDetailDTO.builder()
        .id(res.getId())
        .title(title)
        .startDate(start)
        .endDate(end)
        .dailyCourses(fixed)
        .build();
  }

  private List<CourseResponse.SimpleSpotDTO> normalizeVisitOrderSimple(
      List<CourseResponse.SimpleSpotDTO> spots) {
    if (spots == null || spots.isEmpty()) return List.of();

    List<CourseResponse.SimpleSpotDTO> sorted =
        spots.stream()
            .sorted(
                (a, b) ->
                    Integer.compare(
                        a.getVisitOrder() == null || a.getVisitOrder() <= 0
                            ? Integer.MAX_VALUE
                            : a.getVisitOrder(),
                        b.getVisitOrder() == null || b.getVisitOrder() <= 0
                            ? Integer.MAX_VALUE
                            : b.getVisitOrder()))
            .collect(Collectors.toList());

    List<CourseResponse.SimpleSpotDTO> rebuilt = new ArrayList<>(sorted.size());
    int order = 1;
    for (CourseResponse.SimpleSpotDTO s : sorted) {
      // visitOrder를 재할당하여 순서를 보장
      rebuilt.add(
          CourseResponse.SimpleSpotDTO.builder()
              .spotId(s.getSpotId())
              .visitOrder(order) // 순차적으로 visitOrder 부여
              .name(s.getName())
              .category(s.getCategory())
              .addr1(s.getAddr1())
              .latitude(s.getLatitude())
              .longitude(s.getLongitude())
              .isSealSpot(s.getIsSealSpot())
              .sealSpotId(s.getSealSpotId())
              .build());
      order++;
    }
    return rebuilt;
  }

  private String guessLocationFromReq(List<String> reqLocations, int dayIndex) {
    if (reqLocations == null || reqLocations.isEmpty()) return "미정";
    return reqLocations.get(Math.min(dayIndex, reqLocations.size() - 1));
  }

  private String guessLocationFromSpotsOrReq(
      List<CourseResponse.SimpleSpotDTO> spots, List<String> reqLocations, int dayIndex) {

    if (spots != null && !spots.isEmpty()) {
      Map<String, Long> counts =
          spots.stream()
              .map(CourseResponse.SimpleSpotDTO::getAddr1)
              .filter(Objects::nonNull)
              .map(this::extractSiGun)
              .filter(Objects::nonNull)
              .collect(Collectors.groupingBy(x -> x, Collectors.counting()));

      if (!counts.isEmpty()) {
        return counts.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
      }
    }
    return guessLocationFromReq(reqLocations, dayIndex);
  }

  /** 시/군/구 추출: "경상북도 경주시 ..." -> "경주시" */
  private String extractSiGun(String addr) {
    if (addr == null) return null;
    String[] toks = addr.split("\\s+");
    for (String t : toks) {
      // 수정된 부분 (typo fix)
      if (t.endsWith("시") || t.endsWith("군") || t.endsWith("구")) return t;
    }
    return null;
  }
}
