/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.spot.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfive.gbjs.domain.guide.entity.AudioGuide;
import com.yfive.gbjs.domain.guide.repository.AudioGuideRepository;
import com.yfive.gbjs.domain.spot.dto.response.SpotDetailResponse;
import com.yfive.gbjs.domain.spot.dto.response.SpotResponse;
import com.yfive.gbjs.domain.spot.dto.response.SpotTtsResponse;
import com.yfive.gbjs.domain.spot.exception.SpotErrorStatus;
import com.yfive.gbjs.global.error.exception.CustomException;
import com.yfive.gbjs.global.page.dto.response.PageResponse;
import com.yfive.gbjs.global.page.exception.PageErrorStatus;
import com.yfive.gbjs.global.page.mapper.PageMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotServiceImpl implements SpotService {

  @Value("${openapi.secret.key}")
  private String serviceKey;

  @Value("${tourist.api.url}")
  private String spotApiUrl;

  private final ObjectMapper objectMapper;
  private final RestClient restClient;
  private final PageMapper pageMapper;
  private final AudioGuideRepository audioGuideRepository;

  @Override
  public PageResponse<SpotResponse> getSpotsByKeyword(
      Pageable pageable, String keyword, Double latitude, Double longitude) {

    List<SpotResponse> spotResponses = fetchSpotListByKeyword(keyword, latitude, longitude);

    spotResponses.sort(Comparator.comparing(SpotResponse::getTitle));

    log.info("관광지 가나다순 조회 성공 - 키워드: {}", keyword);
    return paginateSpotResponses(spotResponses, pageable, latitude, longitude);
  }

  @Override
  public PageResponse<SpotResponse> getSpotsByKeywordSortedByDistance(
      Pageable pageable, String keyword, Double latitude, Double longitude) {

    List<SpotResponse> spotResponses = fetchSpotListByKeyword(keyword, latitude, longitude);

    spotResponses.sort(Comparator.comparing(SpotResponse::getDistance));

    log.info("관광지 거리순 조회 성공 - 키워드: {}", keyword);
    return paginateSpotResponses(spotResponses, pageable, latitude, longitude);
  }

  private List<SpotResponse> fetchSpotListByKeyword(
      String keyword, Double latitude, Double longitude) {

    String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
    UriComponentsBuilder uriBuilder =
        UriComponentsBuilder.fromUriString(spotApiUrl + "/searchKeyword2")
            .queryParam("serviceKey", serviceKey)
            .queryParam("numOfRows", 1000)
            .queryParam("pageNo", 1)
            .queryParam("MobileOS", "WEB")
            .queryParam("MobileApp", "gbjs")
            .queryParam("_type", "JSON")
            .queryParam("arrange", "O")
            .queryParam("keyword", encodedKeyword)
            .queryParam("areaCode", "35");

    String response =
        restClient.get().uri(uriBuilder.build(true).toUri()).retrieve().body(String.class);

    validateApiResponse(response, "빈 응답 수신");

    try {
      JsonNode root = objectMapper.readTree(response);
      JsonNode items = root.path("response").path("body").path("items").path("item");

      List<SpotResponse> spotResponses = new ArrayList<>();
      for (JsonNode item : items) {
        SpotResponse spotResponse = objectMapper.treeToValue(item, SpotResponse.class);
        spotResponses.add(spotResponse);

        if (latitude != null
            && longitude != null
            && item.get("mapy") != null
            && item.get("mapx") != null) {
          double distance =
              calculateDistance(
                  latitude, longitude, item.get("mapy").asDouble(), item.get("mapx").asDouble());
          spotResponse.setDistance(distance);
        } else {
          spotResponse.setDistance(null);
        }

        boolean ttsExist = audioGuideRepository.existsByContentId(item.get("contentid").asText());

        spotResponse.setTtsExist(ttsExist);
      }

      return spotResponses;
    } catch (Exception e) {
      log.error("관광지 목록 파싱 실패", e);
      throw new CustomException(SpotErrorStatus.SPOT_API_ERROR);
    }
  }

  @Override
  public SpotDetailResponse getSpotByContentId(
      String contentId, Double latitude, Double longitude) {

    UriComponentsBuilder uriBuilder =
        UriComponentsBuilder.fromUriString(spotApiUrl + "/detailCommon2")
            .queryParam("serviceKey", serviceKey)
            .queryParam("MobileOS", "WEB")
            .queryParam("MobileApp", "gbjs")
            .queryParam("_type", "JSON")
            .queryParam("contentId", contentId);

    String response =
        restClient.get().uri(uriBuilder.build(true).toUri()).retrieve().body(String.class);

    validateApiResponse(response, "빈 응답 수신");

    try {
      JsonNode root = objectMapper.readTree(response);
      JsonNode itemNode = root.path("response").path("body").path("items").path("item");
      if (itemNode.isArray()) {
        if (itemNode.isEmpty()) {
          throw new CustomException(SpotErrorStatus.SPOT_API_ERROR);
        }
        itemNode = itemNode.get(0);
      }

      SpotDetailResponse spotDetailResponse =
          objectMapper.treeToValue(itemNode, SpotDetailResponse.class);

      if (latitude != null
          && longitude != null
          && itemNode.get("mapy") != null
          && itemNode.get("mapx") != null) {
        double distance =
            calculateDistance(
                latitude,
                longitude,
                itemNode.get("mapy").asDouble(),
                itemNode.get("mapx").asDouble());
        spotDetailResponse.setDistance(distance);
      } else {
        spotDetailResponse.setDistance(null);
      }
      spotDetailResponse.setType(
          fetchSpotType(
              itemNode.get("contenttypeid").asText(),
              itemNode.get("cat1").asText(),
              itemNode.get("cat2").asText(),
              itemNode.get("cat3").asText()));

      List<AudioGuide> audioGuides = audioGuideRepository.findByContentId(contentId);

      List<SpotTtsResponse> spotTtsResponses =
          audioGuides.stream()
              .map(
                  guide ->
                      SpotTtsResponse.builder()
                          .guideId(guide.getId())
                          .title(guide.getTitle())
                          .script(guide.getScript())
                          .fileId(
                              guide.getAudioFile() == null ? null : guide.getAudioFile().getId())
                          .build())
              .toList();

      spotDetailResponse.setTtsResponseList(spotTtsResponses);

      return spotDetailResponse;
    } catch (Exception e) {
      log.error("관광지 정보 파싱 실패", e);
      throw new CustomException(SpotErrorStatus.SPOT_API_ERROR);
    }
  }

  private String fetchSpotType(String typeId, String cat1, String cat2, String cat3) {

    UriComponentsBuilder uriBuilder =
        UriComponentsBuilder.fromUriString(spotApiUrl + "/categoryCode2")
            .queryParam("serviceKey", serviceKey)
            .queryParam("MobileOS", "WEB")
            .queryParam("MobileApp", "gbjs")
            .queryParam("contentTypeId", typeId)
            .queryParam("cat1", cat1)
            .queryParam("cat2", cat2)
            .queryParam("cat3", cat3)
            .queryParam("_type", "JSON");

    String response =
        restClient.get().uri(uriBuilder.build(true).toUri()).retrieve().body(String.class);

    validateApiResponse(response, "빈 응답 수신");

    try {
      JsonNode root = objectMapper.readTree(response);
      JsonNode itemNode = root.path("response").path("body").path("items").path("item");
      if (itemNode.isArray()) {
        if (itemNode.isEmpty()) {
          throw new CustomException(SpotErrorStatus.SPOT_API_ERROR);
        }
        itemNode = itemNode.get(0);
      }
      return itemNode.get("name").asText();
    } catch (Exception e) {
      log.error("관광지 분류코드 파싱 실패", e);
      throw new CustomException(SpotErrorStatus.SPOT_API_ERROR);
    }
  }

  private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    final int R = 6371; // 지구 반지름 (km)
    double latDist = Math.toRadians(lat2 - lat1);
    double lonDist = Math.toRadians(lon2 - lon1);
    double a =
        Math.sin(latDist / 2) * Math.sin(latDist / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDist / 2)
                * Math.sin(lonDist / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }

  private void validateApiResponse(String response, String logMessage) {
    if (response == null || response.isBlank()) {
      log.error(logMessage);
      throw new CustomException(SpotErrorStatus.SPOT_API_ERROR);
    }

    if (response.trim().startsWith("<?xml") || response.trim().startsWith("<")) {
      throw new CustomException(SpotErrorStatus.SPOT_API_ERROR);
    }
  }

  private PageResponse<SpotResponse> paginateSpotResponses(
      List<SpotResponse> spotResponses, Pageable pageable, Double latitude, Double longitude) {

    if (pageable.getOffset() > Integer.MAX_VALUE) {
      throw new CustomException(PageErrorStatus.PAGE_NOT_FOUND);
    }

    int start = (int) pageable.getOffset();
    if (start >= spotResponses.size()) {
      throw new CustomException(PageErrorStatus.PAGE_NOT_FOUND);
    }

    int end = Math.min(start + pageable.getPageSize(), spotResponses.size());
    List<SpotResponse> pageContent = spotResponses.subList(start, end);

        pageContent.parallelStream()
            .forEach(
                response -> {
                  SpotDetailResponse detail =
                      getSpotByContentId(response.getSpotId(), latitude, longitude);
                  response.setType(detail.getType());
                });

    // ==================================================================
//    pageContent.stream()
//        .forEach(
//            response -> {
//              try {
//                SpotDetailResponse detail =
//                    getSpotByContentId(response.getSpotId(), latitude, longitude);
//                response.setType(detail.getType());
//              } catch (Exception e) {
//                // 에러가 발생하면 로그만 남기고 다음 항목으로 넘어갑니다.
//                log.warn("상세 정보 조회/타입 설정 실패 - spotId: {}. 이 항목은 건너뜁니다.", response.getSpotId());
//              }
//            });
    // ==================================================================

    Page<SpotResponse> page = new PageImpl<>(pageContent, pageable, spotResponses.size());
    return pageMapper.toSpotPageResponse(page);
  }
}
