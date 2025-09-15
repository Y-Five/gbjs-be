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

  private static String s(Map<String, Object> m, String k) {
    if (m == null) return null;
    Object v = m.get(k);
    return v == null ? null : v.toString();
  }

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

  public CourseResponse.CourseDetailDTO generateAiCourse(
      CourseRequest.CreateCourseRequest request) {
    Objects.requireNonNull(request, "request must not be null");
    Objects.requireNonNull(request.getStartDate(), "startDate must not be null");
    Objects.requireNonNull(request.getEndDate(), "endDate must not be null");
    Objects.requireNonNull(request.getLocations(), "locations must not be null");

    LocalDate start = request.getStartDate();
    LocalDate end = request.getEndDate();
    int expectedDays = (int) ChronoUnit.DAYS.between(start, end) + 1;
    if (expectedDays <= 0) {
      throw new IllegalArgumentException("endDate must be on/after startDate");
    }

    // 1) Qdrant에서 관광지 검색 (하이브리드: 개별 검색 + 통합 검색)
    List<String> locations = request.getLocations();
    int totalTopK = Math.max(150, Math.min(300, expectedDays * 40));
    int topKPerLocation = locations.isEmpty() ? 0 : totalTopK / locations.size();

    List<Document> allRelevantDocuments = new ArrayList<>();

    // 1-1) 각 지역별 개별 검색 실행
    if (topKPerLocation > 0) {
      for (String location : locations) {
        String query = location + " 여행지";
        log.info("Executing individual search for '{}' with topK={}", query, topKPerLocation);
        SearchRequest searchRequest =
            SearchRequest.builder().query(query).topK(topKPerLocation).build();
        allRelevantDocuments.addAll(vectorStore.similaritySearch(searchRequest));
      }
    }

    // 1-2) 전체 지역 통합 검색을 추가로 실행하여 후보군 보충
    String combinedQuery = String.join(" ", locations) + " 여행지";
    log.info("Executing combined search for '{}' with topK={}", combinedQuery, totalTopK);
    SearchRequest combinedSearchRequest =
        SearchRequest.builder().query(combinedQuery).topK(totalTopK).build();
    allRelevantDocuments.addAll(vectorStore.similaritySearch(combinedSearchRequest));

    // 2) 문서 → SimpleSpotDTO 변환 (+서버 측 필터링)
    Map<Long, CourseResponse.SimpleSpotDTO> uniq = new LinkedHashMap<>();
    for (Document doc : allRelevantDocuments) {
      Map<String, Object> md = doc.getMetadata();
      if (md == null) continue;

      String addr1 = s(md, "addr1");
      if (addr1 == null) {
        continue;
      }
      boolean isInRequestedLocation = false;
      for (String loc : request.getLocations()) {
        String simpleLoc = loc.replace("군", "").replace("시", "");
        if (addr1.contains(simpleLoc)) {
          isInRequestedLocation = true;
          break;
        }
      }
      if (!isInRequestedLocation) {
        continue;
      }

      String contentIdStr = s(md, "contentId");
      if (contentIdStr == null) continue;
      Long id;
      try {
        id = Long.valueOf(contentIdStr);
      } catch (NumberFormatException e) {
        continue;
      }

      String type = s(md, "type");
      String entityType = s(md, "entity_type");
      if (!"spot".equals(type)) continue;

      if (!uniq.containsKey(id)) {
        CourseResponse.SimpleSpotDTO dto =
            new CourseResponse.SimpleSpotDTO(
                id,
                null,
                s(md, "name"),
                s(md, "category"),
                addr1,
                d(md, "latitude"),
                d(md, "longitude"),
                "seal_spot".equals(entityType),
                "seal_spot".equals(entityType)
                    ? (s(md, "sealSpotId") == null ? null : Long.valueOf(s(md, "sealSpotId")))
                    : null);
        uniq.put(id, dto);
      }
    }
    List<CourseResponse.SimpleSpotDTO> deduped = new ArrayList<>(uniq.values());

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

    int maxTotal = Math.min(expectedDays * 5, 20);
    String seedBase = start + "|" + end + "|" + String.join(",", request.getLocations());
    Random rand = new Random(seedBase.hashCode());
    java.util.Collections.shuffle(seal, rand);
    java.util.Collections.shuffle(general, rand);

    int sealTarget = (int) Math.round(maxTotal * 0.4);
    sealTarget = Math.max(Math.min(sealTarget, seal.size()), Math.min(2, maxTotal));

    List<CourseResponse.SimpleSpotDTO> spotsForOpenAI = new ArrayList<>();
    if (sealTarget > 0 && !seal.isEmpty()) {
      spotsForOpenAI.addAll(seal.subList(0, Math.min(sealTarget, seal.size())));
    }
    int remain = maxTotal - spotsForOpenAI.size();
    if (remain > 0 && !general.isEmpty()) {
      spotsForOpenAI.addAll(general.subList(0, Math.min(remain, general.size())));
    }
    if (spotsForOpenAI.isEmpty() && !deduped.isEmpty()) {
      spotsForOpenAI.add(deduped.get(0));
    }

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
            - Trip duration: %d days from %s to %s for the regions: %s.
            - CRITICAL RULE: Each day in 'dailyCourses' MUST focus on spots from ONLY ONE of the requested regions. Do NOT mix spots from different regions (e.g., Gyeongju, Andong) on the same day.
            - If the number of days is greater than the number of regions, you CAN assign the same region to multiple days. Distribute the regions as evenly as possible.
            - The 'location' field for each day MUST be the name of the single region you focused on for that day (e.g., "경주시").
            - Up to 5 spots per day. Use ONLY values from the provided spots list verbatim.
            - If not a seal spot: isSealSpot=false, sealSpotId=null.
            - Output compact JSON without extra whitespace.

            Available spots (JSON array):
            %s
            """
            .formatted(
                expectedDays,
                request.getStartDate(),
                request.getEndDate(),
                String.join(", ", request.getLocations()),
                spotsJson);

    log.info(
        "Spots sent to OpenAI ({} total): {}",
        spotsForOpenAI.size(),
        spotsForOpenAI.stream()
            .limit(15)
            .map(s -> s.getName() + "(Seal:" + s.getIsSealSpot() + ")")
            .collect(Collectors.joining(", ")));

    OpenAiChatOptions options =
        OpenAiChatOptions.builder()
            .temperature(0.2)
            .maxCompletionTokens(3072)
            .responseFormat(ResponseFormat.builder().type(ResponseFormat.Type.JSON_OBJECT).build())
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

    try {
      result = parseStrict(cleaned, objectMapper);
    } catch (Exception e) {
      log.warn("JSON 1차 파싱 실패, 복구 시도 진행: {}", e.toString());
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
        repaired = chatClient.prompt().options(options).user(repairPrompt).call().content();
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

    Map<Long, CourseResponse.SimpleSpotDTO> originalSpotMap =
        spotsForOpenAI.stream()
            .collect(Collectors.toMap(CourseResponse.SimpleSpotDTO::getSpotId, spot -> spot));

    result = postFix(result, start, end, request.getLocations(), originalSpotMap);

    if (result.getDailyCourses() == null || result.getDailyCourses().size() != expectedDays) {
      log.warn(
          "LLM returned {} days, expected {}",
          result.getDailyCourses() == null ? 0 : result.getDailyCourses().size(),
          expectedDays);
    }

    return result;
  }

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
      List<String> reqLocations,
      Map<Long, CourseResponse.SimpleSpotDTO> originalSpotMap) {

    int expectedDays = (int) ChronoUnit.DAYS.between(start, end) + 1;

    List<CourseResponse.DailyCourseDTO> inputDays =
        res.getDailyCourses() != null ? res.getDailyCourses() : List.of();

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
      CourseResponse.DailyCourseDTO originalDay = byDate.get(date);

      String location;
      List<CourseResponse.SimpleSpotDTO> finalSpots;

      if (originalDay == null) {
        location = guessLocationFromReq(reqLocations, i);
        finalSpots = List.of();
      } else {
        List<CourseResponse.SimpleSpotDTO> spotsFromAi =
            originalDay.getSpots() != null ? originalDay.getSpots() : List.of();
        List<CourseResponse.SimpleSpotDTO> restoredSpots = new ArrayList<>();

        for (CourseResponse.SimpleSpotDTO aiSpot : spotsFromAi) {
          if (aiSpot.getSpotId() == null) continue;

          CourseResponse.SimpleSpotDTO originalSpot = originalSpotMap.get(aiSpot.getSpotId());

          if (originalSpot != null) {
            restoredSpots.add(
                CourseResponse.SimpleSpotDTO.builder()
                    .spotId(originalSpot.getSpotId())
                    .visitOrder(aiSpot.getVisitOrder())
                    .name(originalSpot.getName())
                    .category(originalSpot.getCategory())
                    .addr1(originalSpot.getAddr1())
                    .latitude(originalSpot.getLatitude())
                    .longitude(originalSpot.getLongitude())
                    .isSealSpot(originalSpot.getIsSealSpot())
                    .sealSpotId(originalSpot.getSealSpotId())
                    .build());
          }
        }

        finalSpots = normalizeVisitOrderSimple(restoredSpots);

        String inferredLocation = guessLocationFromSpotsOrReq(finalSpots, reqLocations, i);
        location =
            (inferredLocation == null || inferredLocation.isBlank())
                ? originalDay.getLocation()
                : inferredLocation;
      }

      CourseResponse.DailyCourseDTO day =
          CourseResponse.DailyCourseDTO.builder()
              .dayNumber(i + 1)
              .date(date)
              .location(location)
              .spots(finalSpots)
              .build();
      fixed.add(day);
    }

    String title;
    if (res.getTitle() == null
        || res.getTitle().isBlank()
        || !res.getTitle().matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")) {
      String locationsString = String.join(", ", reqLocations);
      title = String.format("%s %d일 여행 코스", locationsString, expectedDays);
    } else {
      title = res.getTitle();
    }

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
      rebuilt.add(
          CourseResponse.SimpleSpotDTO.builder()
              .spotId(s.getSpotId())
              .visitOrder(order++)
              .name(s.getName())
              .category(s.getCategory())
              .addr1(s.getAddr1())
              .latitude(s.getLatitude())
              .longitude(s.getLongitude())
              .isSealSpot(s.getIsSealSpot())
              .sealSpotId(s.getSealSpotId())
              .build());
    }
    return rebuilt;
  }

  private String guessLocationFromReq(List<String> reqLocations, int dayIndex) {
    if (reqLocations == null || reqLocations.isEmpty()) return "미정";
    return reqLocations.get(dayIndex % reqLocations.size());
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

  private String extractSiGun(String addr) {
    if (addr == null) return null;
    String[] toks = addr.split("\\s+");
    for (String t : toks) {
      if (t.endsWith("시") || t.endsWith("군") || t.endsWith("구")) return t;
    }
    return null;
  }
}
