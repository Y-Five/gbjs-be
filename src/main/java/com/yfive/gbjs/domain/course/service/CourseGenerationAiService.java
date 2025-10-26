/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.service;

import java.nio.charset.StandardCharsets;
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
import java.util.concurrent.atomic.AtomicInteger;
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

/**
 * AI 기반 여행 코스 생성 서비스 - 하이브리드: 라이트 모드 속도 + 씰(경북씰) 100% 포함 + 재생성 다양화 - 군위군 스팟 부족 시 자동 제외하고 나머지 지역으로
 * 대체
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CourseGenerationAiService {

  private final VectorStore vectorStore;
  private final ChatClient chatClient;
  private final ObjectMapper objectMapper;

  private List<Document> sealSpotsCache; // 씰 스팟 캐시

  private static final boolean LIGHT_MODE = true; // 빠른 응답 모드
  private static final int LLM_PLACES_PER_DAY = 4; // 하루 최대 방문지
  private static final double SAFETY_MARGIN = 1.6; // 기본 여유치(라이트 OFF)
  private static final double LIGHT_SAFETY_MARGIN = 1.3; // 라이트 모드 여유치(작게)
  private static final int LIGHT_TOPK_MIN = 8; // 지역별 검색 최소 개수
  private static final int LIGHT_TOPK_MAX = 12; // 지역별 검색 최대 개수
  private static final int LIGHT_MAX_COMPLETION_TOKENS = 2048; // 응답 길이 상한 (JSON 잘림 방지)
  private static final int REGULAR_SPOT_CAP = 25;

  private static final ExecutorService EXEC =
      Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors()));

  private static final Map<String, AtomicInteger> REROLL_LRU =
      Collections.synchronizedMap(
          new LinkedHashMap<>(128, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, AtomicInteger> eldest) {
              return size() > 1000;
            }
          });

  private long baseSeed(LocalDate start, LocalDate end, List<String> locations) {
    byte[] keyBytes =
        (start + "|" + end + "|" + String.join(",", locations)).getBytes(StandardCharsets.UTF_8);
    long hash = 1469598103934665603L;
    for (byte b : keyBytes) {
      hash ^= (b & 0xff);
      hash *= 1099511628211L;
    }
    return hash;
  }

  private long resolveSeed(LocalDate start, LocalDate end, List<String> locations) {
    String key = start + "|" + end + "|" + String.join(", ", locations);
    AtomicInteger counter = REROLL_LRU.computeIfAbsent(key, k -> new AtomicInteger(0));
    int reroll = counter.getAndIncrement(); // 첫 호출 0, 이후 1,2,...
    long base = baseSeed(start, end, locations);
    long mixed = (base * 1099511628211L) ^ (reroll & 0xffffffffL);
    log.info("AI Course seed resolved. key='{}', reroll={}, seed={}", key, reroll, mixed);
    return mixed;
  }

  private static class SpotForAi {
    public Long spotId;
    public String name;
    public Double latitude;
    public Double longitude;
    public Boolean isSealSpot;

    public SpotForAi(Long id, String name, Double lat, Double lon, Boolean seal) {
      this.spotId = id;
      this.name = name;
      this.latitude = lat;
      this.longitude = lon;
      this.isSealSpot = seal;
    }
  }

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
    List<String> locations = request.getLocations(); // 원본 요청 지역
    if (expectedDays <= 0) {
      throw new IllegalArgumentException("endDate must be on/after startDate");
    }

    // [수정] 람다에서 사용할, 값이 변하지 않는 최종 지역 목록 변수
    final List<String> finalLocations;
    // [신규] 여행일수보다 지역이 많으면, 일수에 맞게 지역 수를 랜덤으로 줄임
    if (locations.size() > expectedDays) {
      log.info(
          "Too many locations ({}) for {} days. Reducing to {} locations.",
          locations.size(),
          expectedDays,
          expectedDays);
      List<String> mutableLocations = new ArrayList<>(locations);
      // 재생성 시 다른 지역이 선택되도록 원본 locations를 시드 기반으로 사용
      Random locationShuffleRand = new Random(resolveSeed(start, end, locations));
      Collections.shuffle(mutableLocations, locationShuffleRand);
      finalLocations = mutableLocations.subList(0, (int) expectedDays);
      log.info("Reduced to locations: {}", finalLocations);
    } else {
      finalLocations = locations;
    }

    List<String> simplifiedLocations =
        finalLocations.stream().map(this::simplifyLocationName).toList();

    // [LIGHT] 필요량 기반 topK (라이트 모드일 때 작게)
    int neededTotalSpots =
        (int)
            Math.ceil(
                expectedDays
                    * LLM_PLACES_PER_DAY
                    * (LIGHT_MODE ? LIGHT_SAFETY_MARGIN : SAFETY_MARGIN));
    int topKPerLocation =
        (LIGHT_MODE)
            ? Math.max(
                LIGHT_TOPK_MIN,
                Math.min(
                    LIGHT_TOPK_MAX,
                    (int)
                        Math.ceil((double) neededTotalSpots / Math.max(1, finalLocations.size()))))
            : 30;

    log.info("[LIGHT_MODE={}]: topKPerLocation={}", LIGHT_MODE, topKPerLocation);

    // 씰 스팟 캐싱 로직
    if (this.sealSpotsCache == null) {
      synchronized (this) {
        if (this.sealSpotsCache == null) { // Double-checked locking
          log.info("Cache empty. Populating seal spots cache...");
          SearchRequest sealSearchRequest =
              SearchRequest.builder()
                  .query("경북 씰 관광지")
                  .topK(200) // 경북 전체 씰 스팟을 모두 가져오기 위한 충분한 값
                  .build();
          this.sealSpotsCache =
              vectorStore.similaritySearch(sealSearchRequest).stream()
                  .filter(doc -> "seal_spot".equals(doc.getMetadata().get("entity_type")))
                  .toList();
          log.info(
              "Seal spots cache populated with {} items.",
              this.sealSpotsCache == null ? 0 : this.sealSpotsCache.size());
        }
      }
    }
    List<Document> allSealSpots = this.sealSpotsCache;
    if (allSealSpots != null) {
      log.info("Retrieved {} seal spots from cache.", allSealSpots.size());
    }

    // 지역별 병렬 검색 (일반 스팟만)
    List<Document> parallelDocuments = new ArrayList<>(allSealSpots); // 씰 스팟 결과를 기본으로 추가
    if (topKPerLocation > 0 && !finalLocations.isEmpty()) {
      ExecutorService executor = EXEC; // 재사용
      List<CompletableFuture<List<Document>>> futures =
          finalLocations.stream()
              .map(
                  location ->
                      CompletableFuture.supplyAsync(
                          () -> {
                            log.info(
                                "Executing parallel search for general spots in '{}' with topK={}",
                                location,
                                topKPerLocation);
                            SearchRequest generalSearch =
                                SearchRequest.builder()
                                    .query(location)
                                    .topK(topKPerLocation)
                                    .build();
                            return vectorStore.similaritySearch(generalSearch);
                          },
                          executor))
              .toList();
      parallelDocuments.addAll(
          futures.stream().map(CompletableFuture::join).flatMap(List::stream).toList());
    }

    // 문서 파싱/중복 제거
    Map<Long, CourseResponse.SimpleSpotDTO> uniq = new LinkedHashMap<>();
    parseAndAddDocuments(parallelDocuments, uniq, simplifiedLocations);

    // [LIGHT] 라이트/하이브리드에서는 combined 검색 스킵 (속도)
    int threshold = expectedDays * 7;
    if (!LIGHT_MODE && uniq.size() < threshold) {
      log.info("Initial results insufficient ({} < {}). Combined search.", uniq.size(), threshold);
      int totalTopKForCombined = 80;
      String combinedQuery = String.join(" ", finalLocations);
      SearchRequest combinedSearchRequest =
          SearchRequest.builder().query(combinedQuery).topK(totalTopKForCombined).build();
      List<Document> combinedDocuments = vectorStore.similaritySearch(combinedSearchRequest);
      parseAndAddDocuments(combinedDocuments, uniq, simplifiedLocations);
    } else if (LIGHT_MODE) {
      log.info("[LIGHT] Skipping combined search to reduce latency.");
    }

    List<CourseResponse.SimpleSpotDTO> deduped = new ArrayList<>(uniq.values());
    Map<String, List<CourseResponse.SimpleSpotDTO>> spotsByLocation =
        deduped.stream()
            .collect(
                Collectors.groupingBy(
                    spot -> findLocationForSpot(spot.getAddr1(), finalLocations),
                    Collectors.toCollection(ArrayList::new)));

    // =========================
    // [GWUNWI] 군위군 스팟 부족 시 제외할 지역 계산
    // =========================
    final int MIN_SPOTS_FOR_LOCATION = 2;
    List<String> effectiveLocations = new ArrayList<>(finalLocations);
    List<String> excludedLocations = new ArrayList<>();
    for (String loc : finalLocations) {
      List<CourseResponse.SimpleSpotDTO> list = spotsByLocation.getOrDefault(loc, List.of());
      int total = (list == null) ? 0 : list.size();
      boolean isGwunwi = loc.contains("군위"); // "군위군", "군위" 등 포괄
      if (isGwunwi && total < MIN_SPOTS_FOR_LOCATION) {
        excludedLocations.add(loc);
      }
    }
    if (!excludedLocations.isEmpty() && effectiveLocations.size() - excludedLocations.size() >= 1) {
      effectiveLocations.removeAll(excludedLocations);
      log.info(
          "[GWUNWI] excludedLocations={}, effectiveLocations={}",
          excludedLocations,
          effectiveLocations);
    } else {
      excludedLocations.clear(); // 제외 보류(유일 지역 등이면)
    }

    // =================================================================
    // ★ [수정됨 2.0] AI 후보 선정 로직: '씰 우선 + 일반 스팟 캡'
    // =================================================================

    // [REROLL] 재생성 시 결과 달라지도록 시드 (제외 반영된 지역을 기준으로)
    // (이 호출은 카운터를 증가시키므로 반드시 실행되어야 함)
    Random rand = new Random(resolveSeed(start, end, effectiveLocations));

    List<CourseResponse.SimpleSpotDTO> spotsForOpenAI = new ArrayList<>();

    // (파라미터 REGULAR_SPOT_CAP = 25 사용)

    // 1. [1순위] 모든 '씰 스팟'을 우선 확보 (중복 제거)
    Map<Long, CourseResponse.SimpleSpotDTO> sealSpotsMap = new LinkedHashMap<>();
    for (String location : effectiveLocations) {
      List<CourseResponse.SimpleSpotDTO> spotsInLocation = spotsByLocation.get(location);
      if (spotsInLocation == null || spotsInLocation.isEmpty()) continue;

      // [★핵심 버그 수정★] Boolean.TRUE.equals 사용 (Null-Safe)
      spotsInLocation.stream()
          .filter(s -> Boolean.TRUE.equals(s.getIsSealSpot()))
          .forEach(s -> sealSpotsMap.putIfAbsent(s.getSpotId(), s));
    }

    // [신규] AI가 날짜별로 씰 스팟을 분배할 수 있도록, 찾은 모든 씰 스팟을 전달
    spotsForOpenAI.addAll(sealSpotsMap.values());

    // 2. [2순위] '일반 스팟' 후보군 확보
    List<CourseResponse.SimpleSpotDTO> regularSpotCandidates = new ArrayList<>();
    for (String location : effectiveLocations) {
      List<CourseResponse.SimpleSpotDTO> spotsInLocation = spotsByLocation.get(location);
      if (spotsInLocation == null || spotsInLocation.isEmpty()) continue;

      spotsInLocation.stream()
          .filter(s -> !Boolean.TRUE.equals(s.getIsSealSpot())) // 씰이 아닌 것
          .forEach(regularSpotCandidates::add);
    }

    // 3. [별도 캡] '위성' 일반 스팟을 'REGULAR_SPOT_CAP' 개수만큼만 채우기
    if (!regularSpotCandidates.isEmpty()) {

      // 기준점: 씰 스팟이 있으면 씰의 중심점, 없으면 일반 스팟의 중심점
      List<CourseResponse.SimpleSpotDTO> referencePoints =
          spotsForOpenAI.isEmpty() ? regularSpotCandidates : spotsForOpenAI;

      double avgLat =
          referencePoints.stream()
              .filter(s -> s.getLatitude() != null)
              .mapToDouble(CourseResponse.SimpleSpotDTO::getLatitude)
              .average()
              .orElse(0.0);
      double avgLon =
          referencePoints.stream()
              .filter(s -> s.getLongitude() != null)
              .mapToDouble(CourseResponse.SimpleSpotDTO::getLongitude)
              .average()
              .orElse(0.0);

      // 가장 가까운 K개(REGULAR_SPOT_CAP)의 일반 스팟을 뽑음
      List<CourseResponse.SimpleSpotDTO> satelliteRegularSpots =
          topKNearest(regularSpotCandidates, avgLat, avgLon, REGULAR_SPOT_CAP);

      // [REROLL] 재생성 시 다양성을 위해 일반 스팟 후보를 섞음
      Collections.shuffle(satelliteRegularSpots, rand);

      spotsForOpenAI.addAll(satelliteRegularSpots);
    }

    // [로그] AI에게 총 몇 개의 스팟을 보내는지 확인
    log.info(
        "[LIGHT] Total spots for AI: {} ({} seals, {} regulars)",
        spotsForOpenAI.size(),
        spotsForOpenAI.size() - regularSpotCandidates.size(), // 이 계산은 정확하지 않을 수 있음
        regularSpotCandidates.size());

    // =================================================================
    // ★ [수정됨 2.0] 로직 끝
    // =================================================================

    // 경량 JSON 직렬화
    List<SpotForAi> compact =
        spotsForOpenAI.stream()
            .map(
                s ->
                    new SpotForAi(
                        s.getSpotId(),
                        s.getName(),
                        s.getLatitude(),
                        s.getLongitude(),
                        s.getIsSealSpot()))
            .toList();

    String spotsJson;
    try {
      spotsJson = objectMapper.writeValueAsString(compact);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("AI 프롬프트 준비 실패: " + e.getMessage());
    }

    // [★수정됨★] 프롬프트: 씰 "반드시 포함" 규칙 강화
    String prompt =
        """
                    다음 제약 조건에 따라 여행 코스를 생성해 주세요.
                    - 여행 기간: %s부터 %s까지 총 %d일간, 여행 지역: %s.
                    - [★ 핵심 규칙 0 (가장 중요) ★]: 응답은 반드시 여행 기간에 해당하는 **총 %d일**의 일정 전체를 포함해야 합니다.
                    - 핵심 규칙 1: 하루 일정에는 요청된 지역 중 단 하나의 지역에 속한 장소들만 포함해야 합니다.
                    - [★ 핵심 규칙 2 (가장 중요) ★]: 각 하루 일정에는, 만약 그날 배정된 지역에 'isSealSpot: true' 관광지가 있다면, **반드시 1개 또는 2개 포함**해야 합니다.
                    - 규칙 3: 각 날짜별 일정은 **반드시 4개**의 관광지를 포함해야 합니다. 씰 관광지와 동선이 효율적인 장소들을 추가하세요.
                    - 규칙 4: 제공된 '사용 가능한 장소 목록'에 있는 정보만 사용해야 합니다.
                    - 응답은 간결하게, 불필요한 설명 없이 결과만 출력해 주세요.

                    사용 가능한 장소 목록 (JSON 배열):
                    %s
                    """
            .formatted(
                start,
                end,
                expectedDays,
                String.join(", ", effectiveLocations),
                expectedDays,
                spotsJson);

    // [수정] 여행 일수에 비례하여 응답 토큰 상한을 동적으로 조절
    int dynamicMaxTokens = Math.max(2048, (int) expectedDays * 500);
    // log.info("Dynamically setting max completion tokens to: {}", dynamicMaxTokens);
    OpenAiChatOptions options =
        OpenAiChatOptions.builder()
            .temperature(0.5)
            .maxCompletionTokens(LIGHT_MODE ? dynamicMaxTokens : 4096)
            .build();

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
      log.error("AI 코스 생성 실패. Prompt size approx: {} bytes", spotsJson.length(), e); // 에러 로그 강화
      throw new RuntimeException("AI 코스 생성에 실패했습니다: " + e.getMessage());
    }

    Map<Long, CourseResponse.SimpleSpotDTO> originalSpotMap =
        spotsForOpenAI.stream()
            .collect(
                Collectors.toMap(
                    CourseResponse.SimpleSpotDTO::getSpotId,
                    spot -> spot,
                    (first, second) -> first));

    // postFix로도 effectiveLocations를 전달하여 제목/후처리에 반영
    return postFix(result, start, end, effectiveLocations, originalSpotMap);
  }

  private void parseAndAddDocuments(
      List<Document> documents,
      Map<Long, CourseResponse.SimpleSpotDTO> uniq,
      List<String> simplifiedLocations) { // simplifiedLocations = ["경주", "안동"]
    for (Document doc : documents) {
      Map<String, Object> md = doc.getMetadata();
      if (md == null) continue;
      String addr1 = s(md, "addr1");
      String locationMeta = s(md, "location"); // "GYEONGJU" 또는 "ANDONG" 같은 값

      // addr1이 null이어도 locationMeta로 검사할 수 있으므로,
      //       둘 다 null일 때만 건너뛰도록 변경 (혹은 addr1만 체크해도 된다면 원복)
      if (addr1 == null && locationMeta == null) continue;

      boolean isInRequestedLocation = false;

      // 1. 주소(addr1) 기반 필터링
      if (addr1 != null) {
        isInRequestedLocation = simplifiedLocations.stream().anyMatch(addr1::contains);
      }

      // 2. 메타데이터(location) 기반 필터링 (i18n 및 대소문자 무시)
      //    주소(addr1)에서 못 찾았을 경우, 'location' 메타데이터를 확인합니다.
      if (!isInRequestedLocation && locationMeta != null) {
        String locMetaLower = locationMeta.toLowerCase(); // "gyeongju"
        isInRequestedLocation =
            simplifiedLocations.stream() // simpleLoc = "경주"
                .anyMatch(
                    simpleLoc -> {
                      String simpleLocLower = simpleLoc.toLowerCase(); // "경주"
                      // "gyeongju"가 "경주"를 포함하거나, "경주"가 "gyeongju"를 포함하는지
                      // (영문/한글 교차 검사)
                      return locMetaLower.contains(simpleLocLower)
                          || simpleLocLower.contains(locMetaLower);
                    });
      }

      // 3. 두 필터 중 하나도 통과 못하면 스킵
      if (!isInRequestedLocation) continue;

      String contentIdStr = s(md, "contentId");
      if (contentIdStr == null) continue;

      try {
        Long id = Long.valueOf(contentIdStr);

        String type = s(md, "type");
        String entityType = s(md, "entity_type");

        if (!"spot".equals(type)) continue;

        boolean isSealSpot = "spot".equals(type) && "seal_spot".equals(entityType);
        Long sealSpotId =
            isSealSpot && s(md, "sealSpotId") != null ? Long.valueOf(s(md, "sealSpotId")) : null;

        // 1. DTO 생성
        CourseResponse.SimpleSpotDTO newSpot =
            new CourseResponse.SimpleSpotDTO(
                id,
                null,
                s(md, "name"),
                s(md, "category"),
                addr1, // addr1이 null일 수 있으나 DTO 스펙상 허용
                d(md, "latitude"),
                d(md, "longitude"),
                isSealSpot,
                sealSpotId);

        // 2. 기존 스팟 조회
        CourseResponse.SimpleSpotDTO existingSpot = uniq.get(id);

        // 3. 씰 스팟 우선 덮어쓰기
        if (existingSpot == null
            || (isSealSpot && !Boolean.TRUE.equals(existingSpot.getIsSealSpot()))) {
          uniq.put(id, newSpot);
        }

      } catch (NumberFormatException e) {
        // ID 파싱 실패 시 건너뛰기
      }
    }
  }

  private String simplifyLocationName(String loc) {
    if (loc == null) return "";
    if (loc.endsWith("시") || loc.endsWith("군") || loc.endsWith("구")) {
      return loc.substring(0, loc.length() - 1);
    }
    return loc;
  }

  private String findLocationForSpot(String addr1, List<String> requestedLocations) {
    if (addr1 == null) return requestedLocations.get(0);
    for (String loc : requestedLocations) {
      String simpleLoc = simplifyLocationName(loc);
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
      List<String> reqLocations, // effectiveLocations가 들어옴
      Map<Long, CourseResponse.SimpleSpotDTO> originalSpotMap) {

    if (aiResult == null || aiResult.getDailyCourses() == null) {
      String locationsString =
          reqLocations.stream().map(this::simplifyLocationName).collect(Collectors.joining(", "));

      return CourseResponse.CourseDetailDTO.builder()
          .title(locationsString + " 코스")
          .startDate(start)
          .endDate(end)
          .dailyCourses(List.of())
          .build();
    }

    List<CourseResponse.DailyCourseDTO> validatedDailyCourses = new ArrayList<>();
    for (CourseResponse.DailyCourseDTO dailyCourse : aiResult.getDailyCourses()) {
      if (dailyCourse.getSpots() == null || dailyCourse.getSpots().isEmpty()) continue;

      List<CourseResponse.SimpleSpotDTO> restoredSpots = new ArrayList<>();
      for (CourseResponse.SimpleSpotDTO aiSpot : dailyCourse.getSpots()) {
        if (aiSpot == null || aiSpot.getSpotId() == null) continue;
        CourseResponse.SimpleSpotDTO originalSpot = originalSpotMap.get(aiSpot.getSpotId());
        if (originalSpot != null) {
          restoredSpots.add(originalSpot.toBuilder().visitOrder(aiSpot.getVisitOrder()).build());
        }
      }

      if (restoredSpots.size() < 3) continue;

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
        reqLocations.stream().map(this::simplifyLocationName).collect(Collectors.joining(", "));

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
              (a.getVisitOrder() == null || a.getVisitOrder() <= 0)
                  ? Integer.MAX_VALUE
                  : a.getVisitOrder();
          int orderB =
              (b.getVisitOrder() == null || b.getVisitOrder() <= 0)
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

  private double fastDist2(double lat1, double lon1, double lat2, double lon2) {
    if (lat1 == 0 || lon1 == 0 || lat2 == 0 || lon2 == 0) return Double.MAX_VALUE;
    double latRad = Math.toRadians((lat1 + lat2) * 0.5);
    double x = Math.toRadians(lon2 - lon1) * Math.cos(latRad);
    double y = Math.toRadians(lat2 - lat1);
    return x * x + y * y;
  }

  // [PERF] 전체 정렬 대신 k-최근만 뽑는 유틸
  private List<CourseResponse.SimpleSpotDTO> topKNearest(
      List<CourseResponse.SimpleSpotDTO> src, double refLat, double refLon, int k) {
    if (src == null || src.isEmpty() || k <= 0) return List.of();

    java.util.PriorityQueue<CourseResponse.SimpleSpotDTO> pq =
        new java.util.PriorityQueue<>(
            Comparator.comparingDouble(
                (CourseResponse.SimpleSpotDTO s) ->
                    -fastDist2(refLat, refLon, s.getLatitude(), s.getLongitude())));

    for (CourseResponse.SimpleSpotDTO s : src) {
      if (s.getLatitude() == null || s.getLongitude() == null) continue;
      if (pq.size() < k) {
        pq.offer(s);
      } else {
        double dNew = fastDist2(refLat, refLon, s.getLatitude(), s.getLongitude());
        CourseResponse.SimpleSpotDTO worst = pq.peek();
        double dWorst = fastDist2(refLat, refLon, worst.getLatitude(), worst.getLongitude());
        if (dNew < dWorst) {
          pq.poll();
          pq.offer(s);
        }
      }
    }

    List<CourseResponse.SimpleSpotDTO> out = new ArrayList<>(pq);
    out.sort(
        Comparator.comparingDouble(
            s -> fastDist2(refLat, refLon, s.getLatitude(), s.getLongitude())));
    return out;
  }
}
