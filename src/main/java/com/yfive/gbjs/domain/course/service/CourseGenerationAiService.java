/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfive.gbjs.domain.course.dto.request.CourseRequest;
import com.yfive.gbjs.domain.course.dto.response.CourseResponse;
import com.yfive.gbjs.domain.course.exception.CourseErrorStatus;
import com.yfive.gbjs.global.error.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** AI 기반 여행 코스 생성 서비스 (확장된 '앵커-위성' 모델 최종 적용) */
@Service
@RequiredArgsConstructor
@Slf4j
public class CourseGenerationAiService {

  private final VectorStore vectorStore;
  private final ChatClient chatClient;
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

    int totalTopK = Math.max(60, Math.min(100, expectedDays * 20));
    int topKPerLocation = locations.isEmpty() ? 0 : totalTopK / locations.size();
    List<Document> allRelevantDocuments = new ArrayList<>();

    if (topKPerLocation > 0 && !locations.isEmpty()) {
      ExecutorService executor = Executors.newCachedThreadPool();
      try {
        List<CompletableFuture<List<Document>>> futures =
            locations.stream()
                .map(
                    location ->
                        CompletableFuture.supplyAsync(
                            () -> {
                              log.info(
                                  "Executing parallel search for '{}' with topK={}",
                                  location,
                                  topKPerLocation);
                              SearchRequest searchRequest =
                                  SearchRequest.builder()
                                      .query(location)
                                      .topK(topKPerLocation)
                                      .build();
                              return vectorStore.similaritySearch(searchRequest);
                            },
                            executor))
                .toList();

        List<Document> parallelResults =
            futures.stream().map(CompletableFuture::join).flatMap(List::stream).toList();
        allRelevantDocuments.addAll(parallelResults);
      } finally {
        executor.shutdown();
      }
    }

    String combinedQuery = String.join(" ", locations);
    SearchRequest combinedSearchRequest =
        SearchRequest.builder().query(combinedQuery).topK(totalTopK).build();
    allRelevantDocuments.addAll(vectorStore.similaritySearch(combinedSearchRequest));

    Map<Long, CourseResponse.SimpleSpotDTO> uniq = new LinkedHashMap<>();
    for (Document doc : allRelevantDocuments) {
      Map<String, Object> md = doc.getMetadata();
      if (md == null) continue;
      String addr1 = s(md, "addr1");
      if (addr1 == null) continue;

      boolean isInRequestedLocation =
          locations.stream()
              .anyMatch(
                  loc -> {
                    String simpleLoc = loc.replaceAll("(시|군|구)$", "");
                    return addr1.contains(simpleLoc);
                  });
      if (!isInRequestedLocation) continue;

      String contentIdStr = s(md, "contentId");
      if (contentIdStr == null) continue;

      try {
        Long id = Long.valueOf(contentIdStr);
        String type = s(md, "type");
        String entityType = s(md, "entity_type");
        if (!"spot".equals(type)) continue;

        if (!uniq.containsKey(id)) {
          boolean isSealSpot = "spot".equals(type) && "seal_spot".equals(entityType);
          Long sealSpotId =
              isSealSpot && s(md, "sealSpotId") != null ? Long.valueOf(s(md, "sealSpotId")) : null;
          uniq.put(
              id,
              new CourseResponse.SimpleSpotDTO(
                  id,
                  null,
                  s(md, "name"),
                  s(md, "category"),
                  addr1,
                  d(md, "latitude"),
                  d(md, "longitude"),
                  isSealSpot,
                  sealSpotId));
        }
      } catch (NumberFormatException e) {
        continue;
      }
    }
    List<CourseResponse.SimpleSpotDTO> deduped = new ArrayList<>(uniq.values());

    Map<String, List<CourseResponse.SimpleSpotDTO>> spotsByLocation =
        deduped.stream()
            .collect(
                Collectors.groupingBy(
                    spot -> findLocationForSpot(spot.getAddr1(), locations),
                    Collectors.toCollection(ArrayList::new)));

    // [최종 수정] 확장된 '앵커-위성' 모델을 사용하여 AI에게 보낼 후보를 지능적으로 선정
    List<CourseResponse.SimpleSpotDTO> spotsForOpenAI = new ArrayList<>();
    Random rand = new Random((start + "|" + end + "|" + String.join(",", locations)).hashCode());

    for (String location : locations) {
      List<CourseResponse.SimpleSpotDTO> spotsInLocation = spotsByLocation.get(location);
      if (spotsInLocation == null || spotsInLocation.isEmpty()) {
        continue;
      }

      Map<Boolean, List<CourseResponse.SimpleSpotDTO>> partitioned =
          spotsInLocation.stream()
              .collect(Collectors.partitioningBy(CourseResponse.SimpleSpotDTO::getIsSealSpot));
      List<CourseResponse.SimpleSpotDTO> sealSpots = partitioned.get(true);
      List<CourseResponse.SimpleSpotDTO> regularSpots = partitioned.get(false);

      List<CourseResponse.SimpleSpotDTO> finalCandidates = new ArrayList<>();

      if (sealSpots.size() >= 2) {
        // Case 1: 띠부실이 2개 이상일 경우 (2-Anchor 모델)
        Collections.shuffle(sealSpots, rand);
        List<CourseResponse.SimpleSpotDTO> anchors = sealSpots.stream().limit(2).toList();

        double midLat = (anchors.get(0).getLatitude() + anchors.get(1).getLatitude()) / 2;
        double midLon = (anchors.get(0).getLongitude() + anchors.get(1).getLongitude()) / 2;

        List<CourseResponse.SimpleSpotDTO> satelliteCandidates =
            regularSpots.stream()
                .sorted(
                    Comparator.comparingDouble(
                        spot ->
                            calculateDistance(
                                midLat, midLon, spot.getLatitude(), spot.getLongitude())))
                .limit(7)
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(satelliteCandidates, rand);
        List<CourseResponse.SimpleSpotDTO> satellites =
            satelliteCandidates.stream().limit(3).toList();

        finalCandidates.addAll(anchors);
        finalCandidates.addAll(satellites);

      } else if (sealSpots.size() == 1) {
        // Case 2: 띠부실이 1개일 경우 (1-Anchor 모델)
        CourseResponse.SimpleSpotDTO anchor = sealSpots.get(0);

        List<CourseResponse.SimpleSpotDTO> satellites =
            regularSpots.stream()
                .sorted(
                    Comparator.comparingDouble(
                        spot ->
                            calculateDistance(
                                anchor.getLatitude(),
                                anchor.getLongitude(),
                                spot.getLatitude(),
                                spot.getLongitude())))
                .limit(4)
                .toList();

        finalCandidates.add(anchor);
        finalCandidates.addAll(satellites);

      } else {
        // Case 3: 띠부실이 하나도 없을 경우 (무게중심 모델)
        if (regularSpots.isEmpty()) continue;

        double avgLat =
            regularSpots.stream()
                .filter(s -> s.getLatitude() != null)
                .mapToDouble(CourseResponse.SimpleSpotDTO::getLatitude)
                .average()
                .orElse(0.0);
        double avgLon =
            regularSpots.stream()
                .filter(s -> s.getLongitude() != null)
                .mapToDouble(CourseResponse.SimpleSpotDTO::getLongitude)
                .average()
                .orElse(0.0);

        List<CourseResponse.SimpleSpotDTO> centeredSpots =
            regularSpots.stream()
                .sorted(
                    Comparator.comparingDouble(
                        spot ->
                            calculateDistance(
                                avgLat, avgLon, spot.getLatitude(), spot.getLongitude())))
                .limit(5)
                .toList();

        finalCandidates.addAll(centeredSpots);
      }
      spotsForOpenAI.addAll(finalCandidates);
    }

    String spotsJson;
    try {
      spotsJson = objectMapper.writeValueAsString(spotsForOpenAI);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("AI 프롬프트 준비 실패: " + e.getMessage());
    }

    String prompt =
        """
                다음 제약 조건에 따라 여행 코스를 생성해 주세요.
                - 여행 기간: %s부터 %s까지 총 %d일간, 여행 지역: %s.
                - 핵심 규칙 1: 하루 일정에는 요청된 지역 중 단 하나의 지역에 속한 장소들만 포함해야 합니다.
                - 핵심 규칙 2: 각 지역별로 가장 매력적인 코스를 만들 수 있도록, 지리적으로 잘 묶인 추천 관광지 목록을 제공합니다. 띠부실 관광지(isSealSpot: true)가 있는 경우 우선적으로 포함되었습니다. 이 장소들을 활용하여 가장 동선이 효율적이고 매력적인 하루 코스를 만들어 주세요.
                - 하루에 최소 3개, 최대 5개의 장소를 방문할 수 있습니다.
                - 제공된 '사용 가능한 장소 목록'에 있는 정보만 사용해야 합니다.

                사용 가능한 장소 목록 (JSON 배열):
                %s
                """
            .formatted(start, end, expectedDays, String.join(", ", locations), spotsJson);

    OpenAiChatOptions options =
        OpenAiChatOptions.builder().temperature(0.2).maxCompletionTokens(3072).build();

    CourseResponse.CourseDetailDTO result;
    try {
      result =
          chatClient
              .prompt()
              .options(options)
              .user(prompt)
              .call()
              .entity(CourseResponse.CourseDetailDTO.class);
    } catch (Exception e) {
      throw new RuntimeException("AI 코스 생성에 실패했습니다: " + e.getMessage());
    }

    Map<Long, CourseResponse.SimpleSpotDTO> originalSpotMap =
        spotsForOpenAI.stream()
            .collect(
                Collectors.toMap(
                    CourseResponse.SimpleSpotDTO::getSpotId,
                    spot -> spot,
                    (first, second) -> first));

    return postFix(result, start, end, locations, originalSpotMap);
  }

  private String findLocationForSpot(String addr1, List<String> requestedLocations) {
    if (addr1 == null) return requestedLocations.get(0);
    for (String loc : requestedLocations) {
      String simpleLoc = loc.replaceAll("(시|군|구)$", "");
      if (addr1.contains(simpleLoc)) {
        return loc;
      }
    }
    return requestedLocations.get(0);
  }

  private CourseResponse.CourseDetailDTO postFix(
      CourseResponse.CourseDetailDTO aiResult,
      LocalDate start,
      LocalDate end,
      List<String> reqLocations,
      Map<Long, CourseResponse.SimpleSpotDTO> originalSpotMap) {

    if (aiResult == null || aiResult.getDailyCourses() == null) {
      String locationsString =
          reqLocations.stream()
              .map(loc -> loc.replaceAll("(시|군|구)$", ""))
              .collect(Collectors.joining(", "));

      return CourseResponse.CourseDetailDTO.builder()
          .title(locationsString + " 코스")
          .startDate(start)
          .endDate(end)
          .dailyCourses(List.of())
          .build();
    }

    List<CourseResponse.DailyCourseDTO> validatedDailyCourses = new ArrayList<>();
    for (CourseResponse.DailyCourseDTO dailyCourse : aiResult.getDailyCourses()) {
      if (dailyCourse.getSpots() == null || dailyCourse.getSpots().isEmpty()) {
        continue;
      }

      List<CourseResponse.SimpleSpotDTO> restoredSpots = new ArrayList<>();
      for (CourseResponse.SimpleSpotDTO aiSpot : dailyCourse.getSpots()) {
        if (aiSpot == null || aiSpot.getSpotId() == null) continue;
        CourseResponse.SimpleSpotDTO originalSpot = originalSpotMap.get(aiSpot.getSpotId());

        if (originalSpot != null) {
          restoredSpots.add(originalSpot.toBuilder().visitOrder(aiSpot.getVisitOrder()).build());
        }
      }

      if (restoredSpots.size() < 2) continue;

      List<CourseResponse.SimpleSpotDTO> finalSpots = normalizeVisitOrderSimple(restoredSpots);

      validatedDailyCourses.add(
          CourseResponse.DailyCourseDTO.builder()
              .dayNumber(dailyCourse.getDayNumber())
              .date(dailyCourse.getDate())
              .location(dailyCourse.getLocation())
              .spots(finalSpots)
              .build());
    }

    List<CourseResponse.DailyCourseDTO> finalDailyCourses = new ArrayList<>();
    for (int i = 0; i < validatedDailyCourses.size(); i++) {
      CourseResponse.DailyCourseDTO course = validatedDailyCourses.get(i);
      finalDailyCourses.add(course.toBuilder().dayNumber(i + 1).build());
    }

    long finalDays = ChronoUnit.DAYS.between(start, end) + 1;

    String locationsString =
        reqLocations.stream()
            .map(loc -> loc.replaceAll("(시|군|구)$", ""))
            .collect(Collectors.joining(", "));

    String title = String.format("%s %d일 코스", locationsString, finalDays);

    return CourseResponse.CourseDetailDTO.builder()
        .id(aiResult.getId())
        .title(title)
        .startDate(start)
        .endDate(end)
        .dailyCourses(finalDailyCourses)
        .build();
  }

  private List<CourseResponse.SimpleSpotDTO> normalizeVisitOrderSimple(
      List<CourseResponse.SimpleSpotDTO> spots) {
    if (spots == null || spots.isEmpty()) return List.of();

    List<CourseResponse.SimpleSpotDTO> sortedSpots = new ArrayList<>(spots);
    sortedSpots.sort(
        (a, b) -> {
          int orderA =
              a.getVisitOrder() == null || a.getVisitOrder() <= 0
                  ? Integer.MAX_VALUE
                  : a.getVisitOrder();
          int orderB =
              b.getVisitOrder() == null || b.getVisitOrder() <= 0
                  ? Integer.MAX_VALUE
                  : b.getVisitOrder();
          return Integer.compare(orderA, orderB);
        });

    List<CourseResponse.SimpleSpotDTO> result = new ArrayList<>();
    for (int i = 0; i < sortedSpots.size(); i++) {
      result.add(sortedSpots.get(i).toBuilder().visitOrder(i + 1).build());
    }
    return result;
  }

  private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    if (lat1 == 0 || lon1 == 0 || lat2 == 0 || lon2 == 0) return Double.MAX_VALUE;

    final int R = 6371; // 지구 반지름 (km)

    double latDistance = Math.toRadians(lat2 - lat1);
    double lonDistance = Math.toRadians(lon2 - lon1);

    double a =
        Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2)
                * Math.sin(lonDistance / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c; // km 단위 거리
  }
}
