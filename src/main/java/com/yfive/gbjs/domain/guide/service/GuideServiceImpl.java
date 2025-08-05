/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfive.gbjs.domain.guide.dto.response.AudioStoryDetailResponse;
import com.yfive.gbjs.domain.guide.dto.response.AudioStoryListResponse;
import com.yfive.gbjs.domain.guide.dto.response.AudioStorySimpleListResponse;
import com.yfive.gbjs.domain.guide.exception.GuideErrorStatus;
import com.yfive.gbjs.global.error.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuideServiceImpl implements GuideService {

  private final RestClient restClient;
  private final ObjectMapper objectMapper;

  @Value("${audio.api.host}")
  private String audioApiHost;

  @Value("${openapi.secret.key}")
  private String serviceKey;

  private URI buildUri(String path, Map<String, Object> queryParams) {
    UriComponentsBuilder builder =
        UriComponentsBuilder.fromHttpUrl(audioApiHost + path)
            .queryParam("serviceKey", serviceKey)
            .queryParam("MobileOS", "ETC")
            .queryParam("MobileApp", "GBJS")
            .queryParam("langCode", "ko")
            .queryParam("_type", "json");

    queryParams.forEach(builder::queryParam);

    return builder.build(true).toUri();
  }

  /** {@inheritDoc} */
  @Override
  public AudioStoryListResponse getAudioStoryBasedList(
      String spotId, String tlid, Integer pageNo, Integer numOfRows) {
    Map<String, Object> params = new java.util.HashMap<>();

    if (spotId != null) {
      // spotId가 있으면 원래대로 페이징 적용
      params.put("pageNo", pageNo);
      params.put("numOfRows", numOfRows);
      params.put("tid", spotId);
    } else {
      // spotId가 없으면 경상북도 필터링을 위해 많은 데이터를 가져옴
      params.put("pageNo", 1);
      params.put("numOfRows", 1000);
    }

    if (tlid != null) {
      params.put("tlid", tlid);
    }

    URI url = buildUri("/storyBasedList", params);

    // spotId가 지정된 경우에는 필터링 없이, 없는 경우에는 경상북도 필터링 적용
    if (spotId != null) {
      return executeApiCall(
          url,
          "audio story based list",
          response -> parseAudioStoryResponseForSpecificSpot(response, pageNo, numOfRows));
    } else {
      return executeApiCall(
          url,
          "audio story based list",
          response -> parseAudioStoryResponse(response, pageNo, numOfRows));
    }
  }

  /** {@inheritDoc} */
  @Override
  public AudioStoryListResponse getAudioStoryLocationBasedList(
      Double longitude, Double latitude, Integer pageNo, Integer numOfRows) {
    Map<String, Object> params = new java.util.HashMap<>();
    params.put("mapX", longitude);
    params.put("mapY", latitude);
    params.put("pageNo", 1); // 경상북도 필터링을 위해 충분한 데이터 가져오기
    params.put("numOfRows", 1000);
    params.put("radius", 999999); // 반경 제한 없이 모든 데이터를 가져오기 위해 매우 큰 값 설정

    URI url = buildUri("/storyLocationBasedList", params);
    return executeApiCall(
        url,
        "audio story location based list",
        response ->
            parseAudioStoryLocationResponse(response, longitude, latitude, pageNo, numOfRows));
  }

  /** {@inheritDoc} */
  @Override
  public AudioStoryListResponse getAudioStorySearchList(
      String keyword, Integer pageNo, Integer numOfRows) {
    URI url =
        buildUri(
            "/storySearchList",
            Map.of(
                "keyword", URLEncoder.encode(keyword, StandardCharsets.UTF_8),
                "pageNo", 1, // 경상북도 필터링을 위해 충분한 데이터 가져오기
                "numOfRows", 1000));
    return executeApiCall(
        url,
        "audio story search list",
        response -> parseAudioStoryResponse(response, pageNo, numOfRows));
  }

  /**
   * 공통 API 호출 로직을 처리합니다.
   *
   * @param url 호출할 API URL
   * @param operation 작업 설명 (로깅용)
   * @param responseParser 응답 파싱 함수
   * @param <T> 반환 타입
   * @return 파싱된 응답 객체
   */
  private <T> T executeApiCall(URI url, String operation, Function<String, T> responseParser) {
    log.info("Requesting {}: {}", operation, url);

    try {
      String response = restClient.get().uri(url).retrieve().body(String.class);
      if (response == null || response.isBlank()) {
        log.warn("가이드 API 응답이 비어있습니다.");
        throw new CustomException(GuideErrorStatus.EMPTY_RESPONSE);
      }
      return responseParser.apply(response);
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error("{} 실패: {}", operation, e.getMessage(), e);
      throw new CustomException(GuideErrorStatus.API_REQUEST_FAILED);
    }
  }

  /**
   * 오디오 스토리 API 응답을 파싱하여 AudioStoryListResponse 객체로 변환합니다.
   *
   * @param response API 응답 JSON 문자열
   * @return 파싱된 오디오 스토리 목록 응답 객체
   * @throws CustomException 응답 파싱 중 오류 발생 시
   */
  private AudioStoryListResponse parseAudioStoryResponse(
      String response, Integer requestedPageNo, Integer requestedNumOfRows) {
    try {
      JsonNode root = objectMapper.readTree(response);
      JsonNode body = root.path("response").path("body");

      if (body.isMissingNode()) {
        log.warn("가이드 API 응답 구조가 올바르지 않습니다.");
        throw new CustomException(GuideErrorStatus.PARSING_ERROR);
      }

      Integer totalCount = body.path("totalCount").asInt(0);
      // API에서 받은 값이 아닌 요청한 값을 사용
      Integer pageNo = requestedPageNo;
      Integer numOfRows = requestedNumOfRows;

      JsonNode items = body.path("items").path("item");
      List<AudioStoryListResponse.AudioStorySpot> audioStorySpots = new ArrayList<>();
      int filteredCount = 0;

      if (items.isArray()) {
        for (JsonNode item : items) {
          // 좌표 값 가져오기
          String mapX = item.path("mapX").asText();
          String mapY = item.path("mapY").asText();

          // 경상북도 지역 좌표 범위 확인 (위도: 35.5~37.5, 경도: 128.0~130.0)
          if (mapX != null && mapY != null && !mapX.isEmpty() && !mapY.isEmpty()) {
            try {
              double longitude = Double.parseDouble(mapX); // mapX는 경도
              double latitude = Double.parseDouble(mapY); // mapY는 위도

              // 디버깅: 좌표 값 출력
              log.info("좌표 확인 - 경도: {}, 위도: {}", longitude, latitude);

              if (latitude >= 35.5
                  && latitude <= 37.5
                  && longitude >= 128.0
                  && longitude <= 130.0) {
                filteredCount++;
                String spotId = item.path("tid").asText();

                AudioStoryListResponse.AudioStorySpot audioStorySpot =
                    AudioStoryListResponse.AudioStorySpot.builder()
                        .spotId(spotId)
                        .tlid(item.path("tlid").asText())
                        .audioStoryId(item.path("stid").asText())
                        .stlid(item.path("stlid").asText())
                        .title(item.path("title").asText())
                        .addr1(item.path("mapX").asText()) // 경도
                        .addr2(item.path("mapY").asText()) // 위도
                        .audioTitle(item.path("audioTitle").asText())
                        .script(item.path("script").asText())
                        .playTime(parsePlayTime(item.path("playTime").asText()))
                        .audioUrl(item.path("audioUrl").asText())
                        .langCode(item.path("langCode").asText())
                        .imageUrl(item.path("imageUrl").asText())
                        .build();
                audioStorySpots.add(audioStorySpot);
              }
            } catch (NumberFormatException e) {
              // 좌표 변환 실패 시 무시
              log.warn("좌표 변환 실패: mapX={}, mapY={}", mapX, mapY);
            }
          }
        }
      }

      log.info("전체 오디오 스토리: {}, 경상북도 필터링 후: {}", items.size(), filteredCount);

      // 페이징 처리
      int startIndex = (pageNo - 1) * numOfRows;
      int endIndex = Math.min(startIndex + numOfRows, audioStorySpots.size());

      List<AudioStoryListResponse.AudioStorySpot> pagedSpots =
          startIndex < audioStorySpots.size()
              ? audioStorySpots.subList(startIndex, endIndex)
              : new ArrayList<>();

      // 첫 페이지와 마지막 페이지 여부 계산
      boolean first = (pageNo == 1);
      boolean last = endIndex >= audioStorySpots.size();

      return AudioStoryListResponse.builder()
          .totalCount(filteredCount) // 필터링된 개수로 설정
          .pageNo(pageNo)
          .pageSize(numOfRows)
          .first(first)
          .last(last)
          .audioSpotList(pagedSpots)
          .build();
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error("오디오 스토리 응답 파싱 실패: {}", e.getMessage(), e);
      throw new CustomException(GuideErrorStatus.PARSING_ERROR);
    }
  }

  /**
   * 재생 시간 문자열을 Integer로 파싱합니다.
   *
   * @param playTime 재생 시간 문자열
   * @return 파싱된 재생 시간 (초 단위), 파싱 실패 시 null
   */
  private Integer parsePlayTime(String playTime) {
    if (playTime == null || playTime.trim().isEmpty()) return null;
    try {
      return Integer.parseInt(playTime.trim());
    } catch (NumberFormatException e) {
      log.warn("재생 시간 파싱 실패: {}", playTime);
      return null;
    }
  }

  /**
   * 특정 관광지의 오디오 스토리 API 응답을 파싱합니다. 특정 spotId 조회 시에는 경상북도 필터링을 적용하지 않습니다.
   *
   * @param response API 응답 JSON 문자열
   * @return 파싱된 오디오 스토리 목록 응답 객체
   * @throws CustomException 응답 파싱 중 오류 발생 시
   */
  private AudioStoryListResponse parseAudioStoryResponseForSpecificSpot(
      String response, Integer pageNo, Integer numOfRows) {
    try {
      JsonNode root = objectMapper.readTree(response);
      JsonNode body = root.path("response").path("body");

      if (body.isMissingNode()) {
        log.warn("가이드 API 응답 구조가 올바르지 않습니다.");
        throw new CustomException(GuideErrorStatus.PARSING_ERROR);
      }

      Integer totalCount = body.path("totalCount").asInt(0);

      JsonNode items = body.path("items").path("item");
      List<AudioStoryListResponse.AudioStorySpot> audioStorySpots = new ArrayList<>();

      if (items.isArray()) {
        for (JsonNode item : items) {
          AudioStoryListResponse.AudioStorySpot audioStorySpot =
              AudioStoryListResponse.AudioStorySpot.builder()
                  .spotId(item.path("tid").asText())
                  .tlid(item.path("tlid").asText())
                  .audioStoryId(item.path("stid").asText())
                  .stlid(item.path("stlid").asText())
                  .title(item.path("title").asText())
                  .addr1(item.path("mapX").asText()) // 경도
                  .addr2(item.path("mapY").asText()) // 위도
                  .audioTitle(item.path("audioTitle").asText())
                  .script(item.path("script").asText())
                  .playTime(parsePlayTime(item.path("playTime").asText()))
                  .audioUrl(item.path("audioUrl").asText())
                  .langCode(item.path("langCode").asText())
                  .imageUrl(item.path("imageUrl").asText())
                  .build();
          audioStorySpots.add(audioStorySpot);
        }
      }

      // 첫 페이지와 마지막 페이지 여부 계산
      boolean first = (pageNo == 1);
      boolean last = (pageNo * numOfRows) >= totalCount;

      return AudioStoryListResponse.builder()
          .totalCount(totalCount)
          .pageNo(pageNo)
          .pageSize(numOfRows)
          .first(first)
          .last(last)
          .audioSpotList(audioStorySpots)
          .build();
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error("특정 관광지 오디오 스토리 응답 파싱 실패: {}", e.getMessage(), e);
      throw new CustomException(GuideErrorStatus.PARSING_ERROR);
    }
  }

  /**
   * 위치기반 오디오 스토리 API 응답을 파싱합니다. 사용자 위치로부터의 거리를 계산하여 가까운 순으로 정렬합니다.
   *
   * @param response API 응답 JSON 문자열
   * @param userLongitude 사용자 경도
   * @param userLatitude 사용자 위도
   * @return 경상북도 지역으로 필터링되고 거리순으로 정렬된 오디오 스토리 목록 응답 객체
   * @throws CustomException 응답 파싱 중 오류 발생 시
   */
  private AudioStoryListResponse parseAudioStoryLocationResponse(
      String response,
      Double userLongitude,
      Double userLatitude,
      Integer requestedPageNo,
      Integer requestedNumOfRows) {
    try {
      JsonNode root = objectMapper.readTree(response);
      JsonNode body = root.path("response").path("body");

      if (body.isMissingNode()) {
        log.warn("가이드 API 응답 구조가 올바르지 않습니다.");
        throw new CustomException(GuideErrorStatus.PARSING_ERROR);
      }

      Integer totalCount = body.path("totalCount").asInt(0);
      // API에서 받은 값이 아닌 요청한 값을 사용
      Integer pageNo = requestedPageNo;
      Integer numOfRows = requestedNumOfRows;

      JsonNode items = body.path("items").path("item");
      List<SpotWithDistance> spotsWithDistance = new ArrayList<>();

      if (items.isArray()) {
        for (JsonNode item : items) {
          String spotId = item.path("tid").asText();

          // 좌표 값 가져오기
          String mapX = item.path("mapX").asText();
          String mapY = item.path("mapY").asText();

          // 경상북도 지역 좌표 범위 확인 (위도: 35.5~37.5, 경도: 128.0~130.0)
          if (mapX != null && mapY != null && !mapX.isEmpty() && !mapY.isEmpty()) {
            try {
              double longitude = Double.parseDouble(mapX); // mapX는 경도
              double latitude = Double.parseDouble(mapY); // mapY는 위도

              if (latitude >= 35.5
                  && latitude <= 37.5
                  && longitude >= 128.0
                  && longitude <= 130.0) {

                // 거리 계산
                double distance =
                    calculateDistance(userLatitude, userLongitude, latitude, longitude);

                AudioStoryListResponse.AudioStorySpot audioStorySpot =
                    AudioStoryListResponse.AudioStorySpot.builder()
                        .spotId(spotId)
                        .tlid(item.path("tlid").asText())
                        .audioStoryId(item.path("stid").asText())
                        .stlid(item.path("stlid").asText())
                        .title(item.path("title").asText())
                        .addr1(item.path("mapX").asText()) // 경도
                        .addr2(item.path("mapY").asText()) // 위도
                        .audioTitle(item.path("audioTitle").asText())
                        .script(item.path("script").asText())
                        .playTime(parsePlayTime(item.path("playTime").asText()))
                        .audioUrl(item.path("audioUrl").asText())
                        .langCode(item.path("langCode").asText())
                        .imageUrl(item.path("imageUrl").asText())
                        .build();
                spotsWithDistance.add(new SpotWithDistance(audioStorySpot, distance));
              }
            } catch (NumberFormatException e) {
              // 좌표 변환 실패 시 무시
              log.warn("좌표 변환 실패: mapX={}, mapY={}", mapX, mapY);
            }
          }
        }
      }

      // 거리순으로 정렬
      spotsWithDistance.sort(Comparator.comparingDouble(SpotWithDistance::getDistance));

      // 정렬된 결과에서 AudioStorySpot 리스트 추출
      List<AudioStoryListResponse.AudioStorySpot> audioStorySpots =
          spotsWithDistance.stream().map(SpotWithDistance::getSpot).toList();

      int filteredCount = audioStorySpots.size();
      log.info("전체 오디오 스토리: {}, 경상북도 필터링 후: {}", items.size(), filteredCount);

      // 페이징 처리
      int startIndex = (pageNo - 1) * numOfRows;
      int endIndex = Math.min(startIndex + numOfRows, audioStorySpots.size());

      List<AudioStoryListResponse.AudioStorySpot> pagedSpots =
          startIndex < audioStorySpots.size()
              ? audioStorySpots.subList(startIndex, endIndex)
              : new ArrayList<>();

      // 첫 페이지와 마지막 페이지 여부 계산
      boolean first = (pageNo == 1);
      boolean last = endIndex >= audioStorySpots.size();

      return AudioStoryListResponse.builder()
          .totalCount(filteredCount) // 필터링된 개수로 설정
          .pageNo(pageNo)
          .pageSize(numOfRows)
          .first(first)
          .last(last)
          .audioSpotList(pagedSpots)
          .build();
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error("위치기반 오디오 스토리 응답 파싱 실패: {}", e.getMessage(), e);
      throw new CustomException(GuideErrorStatus.PARSING_ERROR);
    }
  }

  /**
   * 두 지점 간의 거리를 계산합니다 (Haversine formula 사용).
   *
   * @param lat1 첫 번째 지점의 위도
   * @param lon1 첫 번째 지점의 경도
   * @param lat2 두 번째 지점의 위도
   * @param lon2 두 번째 지점의 경도
   * @return 두 지점 간의 거리 (미터 단위)
   */
  private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    final int EARTH_RADIUS = 6371000; // 지구 반경 (미터)

    double lat1Rad = Math.toRadians(lat1);
    double lat2Rad = Math.toRadians(lat2);
    double deltaLat = Math.toRadians(lat2 - lat1);
    double deltaLon = Math.toRadians(lon2 - lon1);

    double a =
        Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
            + Math.cos(lat1Rad)
                * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2)
                * Math.sin(deltaLon / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return EARTH_RADIUS * c;
  }

  /** 거리 정보와 함께 AudioStorySpot을 저장하는 내부 클래스 */
  private static class SpotWithDistance {
    private final AudioStoryListResponse.AudioStorySpot spot;
    private final double distance;

    public SpotWithDistance(AudioStoryListResponse.AudioStorySpot spot, double distance) {
      this.spot = spot;
      this.distance = distance;
    }

    public AudioStoryListResponse.AudioStorySpot getSpot() {
      return spot;
    }

    public double getDistance() {
      return distance;
    }
  }

  /** {@inheritDoc} */
  @Override
  public AudioStorySimpleListResponse getAudioStorySimpleList(
      String spotId,
      Double longitude,
      Double latitude,
      String keyword,
      Integer pageNo,
      Integer numOfRows) {

    // 기존 메서드 호출하여 전체 데이터 가져오기
    AudioStoryListResponse fullResponse;

    // 키워드 검색
    if (keyword != null && !keyword.trim().isEmpty()) {
      fullResponse = getAudioStorySearchList(keyword, pageNo, numOfRows);
    }
    // 위치기반 조회
    else if (longitude != null && latitude != null) {
      fullResponse = getAudioStoryLocationBasedList(longitude, latitude, pageNo, numOfRows);
    }
    // 기본정보 조회 (spotId 유무와 관계없이)
    else {
      fullResponse = getAudioStoryBasedList(spotId, null, pageNo, numOfRows);
    }

    // 간략한 정보만 추출하여 새로운 응답 객체 생성
    List<AudioStorySimpleListResponse.AudioStorySimpleSpot> simpleSpots =
        fullResponse.getAudioSpotList().stream()
            .map(
                spot ->
                    AudioStorySimpleListResponse.AudioStorySimpleSpot.builder()
                        .spotId(spot.getSpotId())
                        .title(spot.getTitle())
                        .addr1(spot.getAddr1())
                        .addr2(spot.getAddr2())
                        .audioUrl(spot.getAudioUrl())
                        .imageUrl(spot.getImageUrl())
                        .build())
            .toList();

    return AudioStorySimpleListResponse.builder()
        .totalCount(fullResponse.getTotalCount())
        .pageNo(fullResponse.getPageNo())
        .pageSize(fullResponse.getPageSize())
        .first(fullResponse.getFirst())
        .last(fullResponse.getLast())
        .audioSpotList(simpleSpots)
        .build();
  }

  /** {@inheritDoc} */
  @Override
  public AudioStoryDetailResponse getAudioStoryDetail(String spotId) {
    if (spotId == null || spotId.trim().isEmpty()) {
      throw new CustomException(GuideErrorStatus.INVALID_PARAMETER);
    }

    // 페이징 없이 모든 스토리 가져오기 (최대 100개)
    AudioStoryListResponse fullResponse = getAudioStoryBasedList(spotId, null, 1, 100);

    // 상세 정보 추출
    List<AudioStoryDetailResponse.AudioStoryDetail> detailList =
        fullResponse.getAudioSpotList().stream()
            .map(
                spot ->
                    AudioStoryDetailResponse.AudioStoryDetail.builder()
                        .spotId(spot.getSpotId())
                        .audioStoryId(spot.getAudioStoryId())
                        .title(spot.getTitle())
                        .addr1(spot.getAddr1())
                        .addr2(spot.getAddr2())
                        .audioTitle(spot.getAudioTitle())
                        .script(spot.getScript())
                        .playTime(spot.getPlayTime())
                        .audioUrl(spot.getAudioUrl())
                        .imageUrl(spot.getImageUrl())
                        .build())
            .toList();

    return AudioStoryDetailResponse.builder().audioSpotList(detailList).build();
  }
}
