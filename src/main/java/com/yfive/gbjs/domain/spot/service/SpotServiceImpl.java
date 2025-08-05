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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfive.gbjs.domain.spot.dto.response.SpotResponse;
import com.yfive.gbjs.domain.spot.exception.SpotErrorStatus;
import com.yfive.gbjs.global.common.response.PageResponse;
import com.yfive.gbjs.global.error.exception.CustomException;

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

  @Override
  public PageResponse<SpotResponse> getSpotsByKeyword(
      Pageable pageable, String keyword, Double latitude, Double longitude) {

    List<SpotResponse> spotResponses =
        fetchSpotListByKeyword(
            pageable.getPageSize(), pageable.getPageNumber(), keyword, latitude, longitude);

    PageImpl<SpotResponse> page = new PageImpl<>(spotResponses, pageable, spotResponses.size());

    for (int i = 0; i < page.getSize(); i++) {
      SpotResponse spotResponse =
          getSpotByContentId(page.getContent().get(i).getSpotId(), latitude, longitude);

      page.getContent().get(i).setOverview(spotResponse.getOverview());
      page.getContent().get(i).setType(spotResponse.getType());
    }

    return PageResponse.<SpotResponse>builder()
        .content(page.getContent())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .pageNum(page.getNumber())
        .pageSize(page.getSize())
        .last(page.isLast())
        .first(page.isFirst())
        .build();
  }

  @Override
  public PageResponse<SpotResponse> getSpotsByKeywordSortedByDistance(
      Pageable pageable, String keyword, Double latitude, Double longitude) {

    List<SpotResponse> spotResponses =
        fetchSpotListByKeyword(1000, 1, keyword, latitude, longitude);

    spotResponses.sort(Comparator.comparing(SpotResponse::getDistance));

    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), spotResponses.size());

    List<SpotResponse> pageContent = spotResponses.subList(start, end);

    for (int i = 0; i < pageContent.size(); i++) {
      SpotResponse spotResponse =
          getSpotByContentId(pageContent.get(i).getSpotId(), latitude, longitude);

      pageContent.get(i).setOverview(spotResponse.getOverview());
      pageContent.get(i).setType(spotResponse.getType());
    }

    PageImpl<SpotResponse> page = new PageImpl<>(pageContent, pageable, spotResponses.size());

    return PageResponse.<SpotResponse>builder()
        .content(page.getContent())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .pageNum(page.getNumber())
        .pageSize(page.getSize())
        .last(page.isLast())
        .first(page.isFirst())
        .build();
  }

  private List<SpotResponse> fetchSpotListByKeyword(
      Integer pageSize, Integer pageNum, String keyword, Double latitude, Double longitude) {

    String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
    UriComponentsBuilder uriBuilder =
        UriComponentsBuilder.fromHttpUrl(spotApiUrl + "/searchKeyword2")
            .queryParam("serviceKey", serviceKey)
            .queryParam("numOfRows", pageSize)
            .queryParam("pageNo", pageNum)
            .queryParam("MobileOS", "WEB")
            .queryParam("MobileApp", "gbjs")
            .queryParam("_type", "JSON")
            .queryParam("arrange", "O")
            .queryParam("keyword", encodedKeyword)
            .queryParam("areaCode", "35");

    String response =
        restClient.get().uri(uriBuilder.build(true).toUri()).retrieve().body(String.class);

    log.info("response={}", response);

    if (response == null || response.isBlank()) {
      log.error("빈 응답 수신");
      throw new CustomException(SpotErrorStatus.SPOT_API_ERROR);
    }

    if (response.trim().startsWith("<?xml") || response.trim().startsWith("<")) {
      throw new CustomException(SpotErrorStatus.SPOT_API_ERROR);
    }

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

        spotResponse.setAudio(false);
      }

      return spotResponses;
    } catch (Exception e) {
      log.error("관광지 목록 파싱 실패", e);
      throw new CustomException(SpotErrorStatus.SPOT_API_ERROR);
    }
  }

  @Override
  public SpotResponse getSpotByContentId(String contentId, Double latitude, Double longitude) {

    UriComponentsBuilder uriBuilder =
        UriComponentsBuilder.fromHttpUrl(spotApiUrl + "/detailCommon2")
            .queryParam("serviceKey", serviceKey)
            .queryParam("MobileOS", "WEB")
            .queryParam("MobileApp", "gbjs")
            .queryParam("_type", "JSON")
            .queryParam("contentId", contentId);

    String response =
        restClient.get().uri(uriBuilder.build(true).toUri()).retrieve().body(String.class);

    if (response == null || response.isBlank()) {
      log.error("빈 응답 수신");
      throw new CustomException(SpotErrorStatus.SPOT_API_ERROR);
    }

    if (response.trim().startsWith("<?xml") || response.trim().startsWith("<")) {
      throw new CustomException(SpotErrorStatus.SPOT_API_ERROR);
    }

    try {
      JsonNode root = objectMapper.readTree(response);
      JsonNode itemNode = root.path("response").path("body").path("items").path("item");
      if (itemNode.isArray()) {
        if (itemNode.isEmpty()) {
          throw new CustomException(SpotErrorStatus.SPOT_API_ERROR);
        }
        itemNode = itemNode.get(0);
      }

      SpotResponse spotResponse = objectMapper.treeToValue(itemNode, SpotResponse.class);

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
        spotResponse.setDistance(distance);
      } else {
        spotResponse.setDistance(null);
      }
      spotResponse.setAudio(false);
      spotResponse.setType(
          fetchSpotType(
              itemNode.get("contenttypeid").asText(),
              itemNode.get("cat1").asText(),
              itemNode.get("cat2").asText(),
              itemNode.get("cat3").asText()));

      return spotResponse;
    } catch (Exception e) {
      log.error("관광지 정보 파싱 실패", e);
      throw new CustomException(SpotErrorStatus.SPOT_API_ERROR);
    }
  }

  private String fetchSpotType(String typeId, String cat1, String cat2, String cat3) {

    UriComponentsBuilder uriBuilder =
        UriComponentsBuilder.fromHttpUrl(spotApiUrl + "/categoryCode2")
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

    if (response == null || response.isBlank()) {
      log.error("빈 응답 수신");
      throw new CustomException(SpotErrorStatus.SPOT_API_ERROR);
    }

    if (response.trim().startsWith("<?xml") || response.trim().startsWith("<")) {
      throw new CustomException(SpotErrorStatus.SPOT_API_ERROR);
    }

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
}
