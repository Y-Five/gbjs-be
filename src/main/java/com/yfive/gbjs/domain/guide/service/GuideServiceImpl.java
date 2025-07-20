/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfive.gbjs.domain.guide.dto.response.AudioStoryListResponse;
import com.yfive.gbjs.domain.guide.dto.response.GuideListResponse;
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
  public GuideListResponse getThemeBasedList(Integer pageNo, Integer numOfRows) {
    URI url =
        buildUri(
            "/themeBasedList",
            Map.of(
                "pageNo", pageNo,
                "numOfRows", numOfRows));
    return executeApiCall(url, "theme based list", this::parseThemeResponse);
  }

  /** {@inheritDoc} */
  @Override
  public GuideListResponse getThemeLocationBasedList(
      Double longitude, Double latitude, Integer radius, Integer pageNo, Integer numOfRows) {
    URI url =
        buildUri(
            "/themeLocationBasedList",
            Map.of(
                "mapX", longitude,
                "mapY", latitude,
                "radius", radius,
                "pageNo", pageNo,
                "numOfRows", numOfRows));
    return executeApiCall(url, "theme location based list", this::parseThemeResponse);
  }

  /** {@inheritDoc} */
  @Override
  public GuideListResponse getThemeSearchList(String keyword, Integer pageNo, Integer numOfRows) {
    URI url =
        buildUri(
            "/themeSearchList",
            Map.of(
                "keyword", URLEncoder.encode(keyword, StandardCharsets.UTF_8),
                "pageNo", pageNo,
                "numOfRows", numOfRows));
    return executeApiCall(url, "theme search list", this::parseThemeResponse);
  }

  /** {@inheritDoc} */
  @Override
  public AudioStoryListResponse getAudioStoryBasedList(
      String themeId, Integer pageNo, Integer numOfRows) {
    URI url =
        buildUri(
            "/storyBasedList",
            Map.of(
                "tid", themeId,
                "pageNo", pageNo,
                "numOfRows", numOfRows));
    return executeApiCall(url, "audio story based list", this::parseAudioStoryResponse);
  }

  /** {@inheritDoc} */
  @Override
  public AudioStoryListResponse getAudioStoryLocationBasedList(
      Double longitude, Double latitude, Integer radius, Integer pageNo, Integer numOfRows) {
    URI url =
        buildUri(
            "/storyLocationBasedList",
            Map.of(
                "mapX", longitude,
                "mapY", latitude,
                "radius", radius,
                "pageNo", pageNo,
                "numOfRows", numOfRows));
    return executeApiCall(url, "audio story location based list", this::parseAudioStoryResponse);
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
                "pageNo", pageNo,
                "numOfRows", numOfRows));
    return executeApiCall(url, "audio story search list", this::parseAudioStoryResponse);
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
   * 관광지 테마 API 응답을 파싱하여 GuideListResponse 객체로 변환합니다.
   *
   * @param response API 응답 JSON 문자열
   * @return 파싱된 관광지 목록 응답 객체
   * @throws CustomException 응답 파싱 중 오류 발생 시
   */
  private GuideListResponse parseThemeResponse(String response) {
    try {
      JsonNode root = objectMapper.readTree(response);
      JsonNode body = root.path("response").path("body");

      if (body.isMissingNode()) {
        log.warn("가이드 API 응답 구조가 올바르지 않습니다.");
        throw new CustomException(GuideErrorStatus.PARSING_ERROR);
      }

      Integer totalCount = body.path("totalCount").asInt(0);
      Integer pageNo = body.path("pageNo").asInt(1);
      Integer numOfRows = body.path("numOfRows").asInt(0);

      JsonNode items = body.path("items").path("item");
      List<GuideListResponse.GuideItem> guideItems = new ArrayList<>();

      if (items.isArray()) {
        for (JsonNode item : items) {
          GuideListResponse.GuideItem guideItem =
              GuideListResponse.GuideItem.builder()
                  .themeId(item.path("tid").asText())
                  .title(item.path("title").asText())
                  .address(buildAddress(item.path("addr1").asText(), item.path("addr2").asText()))
                  .description(item.path("themaCategory").asText())
                  .latitude(item.path("mapY").asDouble())
                  .longitude(item.path("mapX").asDouble())
                  .imageUrl(item.path("imageUrl").asText())
                  .build();
          guideItems.add(guideItem);
        }
      }

      return GuideListResponse.builder()
          .totalCount(totalCount)
          .pageNo(pageNo)
          .numOfRows(numOfRows)
          .items(guideItems)
          .build();
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error("관광지 응답 파싱 실패: {}", e.getMessage(), e);
      throw new CustomException(GuideErrorStatus.PARSING_ERROR);
    }
  }

  /**
   * 오디오 스토리 API 응답을 파싱하여 AudioStoryListResponse 객체로 변환합니다.
   *
   * @param response API 응답 JSON 문자열
   * @return 파싱된 오디오 스토리 목록 응답 객체
   * @throws CustomException 응답 파싱 중 오류 발생 시
   */
  private AudioStoryListResponse parseAudioStoryResponse(String response) {
    try {
      JsonNode root = objectMapper.readTree(response);
      JsonNode body = root.path("response").path("body");

      if (body.isMissingNode()) {
        log.warn("가이드 API 응답 구조가 올바르지 않습니다.");
        throw new CustomException(GuideErrorStatus.PARSING_ERROR);
      }

      Integer totalCount = body.path("totalCount").asInt(0);
      Integer pageNo = body.path("pageNo").asInt(1);
      Integer numOfRows = body.path("numOfRows").asInt(0);

      JsonNode items = body.path("items").path("item");
      List<AudioStoryListResponse.AudioStoryItem> audioStoryItems = new ArrayList<>();

      if (items.isArray()) {
        for (JsonNode item : items) {
          AudioStoryListResponse.AudioStoryItem audioStoryItem =
              AudioStoryListResponse.AudioStoryItem.builder()
                  .audioStoryId(item.path("stid").asText())
                  .themeId(item.path("tid").asText())
                  .title(item.path("title").asText())
                  .content(item.path("script").asText())
                  .category(item.path("audioTitle").asText())
                  .audioUrl(item.path("audioUrl").asText())
                  .playTime(parsePlayTime(item.path("playTime").asText()))
                  .language(item.path("langCode").asText())
                  .latitude(null)
                  .longitude(null)
                  .build();
          audioStoryItems.add(audioStoryItem);
        }
      }

      return AudioStoryListResponse.builder()
          .totalCount(totalCount)
          .pageNo(pageNo)
          .numOfRows(numOfRows)
          .items(audioStoryItems)
          .build();
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error("오디오 스토리 응답 파싱 실패: {}", e.getMessage(), e);
      throw new CustomException(GuideErrorStatus.PARSING_ERROR);
    }
  }

  /**
   * 주소1과 주소2를 결합하여 완전한 주소 문자열을 생성합니다.
   *
   * @param addr1 기본 주소
   * @param addr2 상세 주소
   * @return 결합된 주소 문자열
   */
  private String buildAddress(String addr1, String addr2) {
    if (addr1 == null || addr1.isBlank()) return addr2;
    if (addr2 == null || addr2.isBlank()) return addr1;
    return addr1 + " " + addr2;
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
}
