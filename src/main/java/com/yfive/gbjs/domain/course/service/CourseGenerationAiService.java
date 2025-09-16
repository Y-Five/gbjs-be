/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
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
import com.yfive.gbjs.domain.course.exception.CourseErrorStatus;
import com.yfive.gbjs.domain.seal.repository.SealSpotRepository;
import com.yfive.gbjs.domain.spot.service.SpotService;
import com.yfive.gbjs.global.error.exception.CustomException;

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
    LocalDate today = LocalDate.now();
    if (request.getStartDate().isBefore(today) || request.getEndDate().isBefore(today)) {
      throw new CustomException(CourseErrorStatus.PAST_DATE_NOT_ALLOWED);
    }
    Objects.requireNonNull(request, "request must not be null");
    LocalDate start = request.getStartDate();
    LocalDate end = request.getEndDate();
    int expectedDays = (int) ChronoUnit.DAYS.between(start, end) + 1;
    List<String> locations = request.getLocations();
    if (expectedDays <= 0) {
      throw new IllegalArgumentException("endDate must be on/after startDate");
    }

    // 1) Qdrant에서 관광지 검색 (하이브리드: 개별 검색 + 통합 검색)
    int totalTopK = Math.max(150, Math.min(300, expectedDays * 40));
    int topKPerLocation = locations.isEmpty() ? 0 : totalTopK / locations.size();
    List<Document> allRelevantDocuments = new ArrayList<>();

    if (topKPerLocation > 0) {
      for (String location : locations) {
        String query = location;
        log.info("Executing individual search for '{}' with topK={}", query, topKPerLocation);
        SearchRequest searchRequest =
            SearchRequest.builder().query(query).topK(topKPerLocation).build();
        allRelevantDocuments.addAll(vectorStore.similaritySearch(searchRequest));
      }
    }
    String combinedQuery = String.join(" ", locations);
    log.info("Executing combined search for '{}' with topK={}", combinedQuery, totalTopK);
    SearchRequest combinedSearchRequest =
        SearchRequest.builder().query(combinedQuery).topK(totalTopK).build();
    allRelevantDocuments.addAll(vectorStore.similaritySearch(combinedSearchRequest));

    // 2) 문서 → DTO 변환 및 필터링
    Map<Long, CourseResponse.SimpleSpotDTO> uniq = new LinkedHashMap<>();
    for (Document doc : allRelevantDocuments) {
      Map<String, Object> md = doc.getMetadata();
      if (md == null) continue;
      String addr1 = s(md, "addr1");
      if (addr1 == null) continue;
      boolean isInRequestedLocation = false;
      for (String loc : locations) {
        String simpleLoc = loc.replace("군", "").replace("시", "");
        if (addr1.contains(simpleLoc)) {
          isInRequestedLocation = true;
          break;
        }
      }
      if (!isInRequestedLocation) continue;

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
                "spot".equals(type) && "seal_spot".equals(entityType),
                "spot".equals(type) && "seal_spot".equals(entityType)
                    ? (s(md, "sealSpotId") == null ? null : Long.valueOf(s(md, "sealSpotId")))
                    : null);
        uniq.put(id, dto);
      }
    }
    List<CourseResponse.SimpleSpotDTO> deduped = new ArrayList<>(uniq.values());

    // 3) 지역별 할당량 기반으로 AI에게 보낼 최종 후보 선정
    Map<String, List<CourseResponse.SimpleSpotDTO>> spotsByLocation =
        deduped.stream()
            .collect(
                Collectors.groupingBy(spot -> findLocationForSpot(spot.getAddr1(), locations)));

    Random rand = new Random((start + "|" + end + "|" + String.join(",", locations)).hashCode());
    spotsByLocation.values().forEach(list -> Collections.shuffle(list, rand));

    List<CourseResponse.SimpleSpotDTO> spotsForOpenAI = new ArrayList<>();
    int maxTotal = Math.min(expectedDays * 5, 20);
    int spotsPerLocationQuota =
        locations.isEmpty() ? 0 : (int) Math.ceil((double) maxTotal / locations.size());

    for (String location : locations) {
      List<CourseResponse.SimpleSpotDTO> spotsInLocation = spotsByLocation.get(location);
      if (spotsInLocation != null && !spotsInLocation.isEmpty()) {
        int countToAdd = Math.min(spotsInLocation.size(), spotsPerLocationQuota);
        spotsForOpenAI.addAll(spotsInLocation.subList(0, countToAdd));
      }
    }

    if (spotsForOpenAI.size() < maxTotal) {
      List<CourseResponse.SimpleSpotDTO> remainingSpots =
          deduped.stream()
              .filter(spot -> !spotsForOpenAI.contains(spot))
              .collect(Collectors.toList());
      Collections.shuffle(remainingSpots, rand);
      int needed = maxTotal - spotsForOpenAI.size();
      if (needed > 0 && !remainingSpots.isEmpty()) {
        spotsForOpenAI.addAll(remainingSpots.subList(0, Math.min(needed, remainingSpots.size())));
      }
    }

    // 4) 프롬프트 구성
    String spotsJson;
    try {
      spotsJson = objectMapper.writeValueAsString(spotsForOpenAI);
    } catch (JsonProcessingException e) {
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

    // 5) AI 호출 및 후처리
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
    result = postFix(result, start, end, locations, originalSpotMap);

    return result;
  }

  private String findLocationForSpot(String addr1, List<String> requestedLocations) {
    if (addr1 == null) return requestedLocations.get(0);
    for (String loc : requestedLocations) {
      String simpleLoc = loc.replace("군", "").replace("시", "");
      if (addr1.contains(simpleLoc)) {
        return loc;
      }
    }
    return requestedLocations.get(0);
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
      fixed.add(
          CourseResponse.DailyCourseDTO.builder()
              .dayNumber(i + 1)
              .date(date)
              .location(location)
              .spots(finalSpots)
              .build());
    }

    int minSpotsPerDay = 2;
    List<CourseResponse.DailyCourseDTO> filteredCourses =
        fixed.stream()
            .filter(dailyCourse -> dailyCourse.getSpots().size() >= minSpotsPerDay)
            .collect(Collectors.toList());

    // [최종 수정] setDayNumber 오류를 해결하기 위해, dayNumber를 재설정한 새로운 리스트를 생성
    List<CourseResponse.DailyCourseDTO> finalDailyCourses = new ArrayList<>();
    for (int i = 0; i < filteredCourses.size(); i++) {
      CourseResponse.DailyCourseDTO originalCourse = filteredCourses.get(i);
      finalDailyCourses.add(
          CourseResponse.DailyCourseDTO.builder()
              .dayNumber(i + 1) // 새로운 dayNumber 부여
              .date(originalCourse.getDate())
              .location(originalCourse.getLocation())
              .spots(originalCourse.getSpots())
              .build());
    }

    // [최종 수정] 제목 생성 로직을 사용자가 처음 요청한 지역 기반으로 고정
    String locationsString = String.join(", ", reqLocations);
    String title = String.format("%s %d일 여행 코스", locationsString, expectedDays);

    return CourseResponse.CourseDetailDTO.builder()
        .id(res.getId())
        .title(title)
        .startDate(start)
        .endDate(end)
        .dailyCourses(finalDailyCourses)
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
