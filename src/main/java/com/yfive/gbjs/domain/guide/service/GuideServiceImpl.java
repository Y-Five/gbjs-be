/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfive.gbjs.domain.guide.dto.response.AudioStoryListResponse;
import com.yfive.gbjs.domain.guide.dto.response.GuideListResponse;
import com.yfive.gbjs.domain.guide.exception.GuideErrorStatus;
import com.yfive.gbjs.global.error.exception.CustomException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

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

  @Override
  public GuideListResponse getThemeBasedList(Integer pageNo, Integer numOfRows) {
    URI url =
        UriComponentsBuilder.fromHttpUrl(audioApiHost + "/themeBasedList")
            .queryParam("serviceKey", serviceKey)
            .queryParam("MobileOS", "ETC")
            .queryParam("MobileApp", "GBJS")
            .queryParam("pageNo", pageNo)
            .queryParam("numOfRows", numOfRows)
            .queryParam("langCode", "ko")
            .queryParam("_type", "json")
            .build(true)
            .toUri();

    log.info("Requesting theme based list: {}", url);

    try {
      String response = restClient.get().uri(url).retrieve().body(String.class);

      if (response == null || response.isBlank()) {
        log.warn("가이드 API 응답이 비어있습니다.");
        throw new CustomException(GuideErrorStatus.EMPTY_RESPONSE);
      }

      return parseThemeResponse(response);

    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error(
          "관광지 기본 정보 목록 조회 실패: pageNo={}, numOfRows={}, error={}",
          pageNo,
          numOfRows,
          e.getMessage(),
          e);
      throw new CustomException(GuideErrorStatus.API_REQUEST_FAILED);
    }
  }

  @Override
  public GuideListResponse getThemeLocationBasedList(
      Double longitude, Double latitude, Integer radius, Integer pageNo, Integer numOfRows) {
    URI url =
        UriComponentsBuilder.fromHttpUrl(audioApiHost + "/themeLocationBasedList")
            .queryParam("serviceKey", serviceKey)
            .queryParam("MobileOS", "ETC")
            .queryParam("MobileApp", "GBJS")
            .queryParam("mapX", longitude)
            .queryParam("mapY", latitude)
            .queryParam("radius", radius)
            .queryParam("pageNo", pageNo)
            .queryParam("numOfRows", numOfRows)
            .queryParam("langCode", "ko")
            .queryParam("_type", "json")
            .build(true)
            .toUri();

    log.info("Requesting theme location based list: {}", url);

    try {
      String response = restClient.get().uri(url).retrieve().body(String.class);

      if (response == null || response.isBlank()) {
        log.warn("가이드 API 응답이 비어있습니다.");
        throw new CustomException(GuideErrorStatus.EMPTY_RESPONSE);
      }

      return parseThemeResponse(response);

    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error(
          "관광지 위치기반 목록 조회 실패: longitude={}, latitude={}, radius={}, error={}",
          longitude,
          latitude,
          radius,
          e.getMessage(),
          e);
      throw new CustomException(GuideErrorStatus.API_REQUEST_FAILED);
    }
  }

  @Override
  public GuideListResponse getThemeSearchList(String keyword, Integer pageNo, Integer numOfRows) {
    URI url =
        UriComponentsBuilder.fromHttpUrl(audioApiHost + "/themeSearchList")
            .queryParam("serviceKey", serviceKey)
            .queryParam("MobileOS", "ETC")
            .queryParam("MobileApp", "GBJS")
            .queryParam("keyword", URLEncoder.encode(keyword, StandardCharsets.UTF_8))
            .queryParam("pageNo", pageNo)
            .queryParam("numOfRows", numOfRows)
            .queryParam("langCode", "ko")
            .queryParam("_type", "json")
            .build(true)
            .toUri();

    log.info("Requesting theme search list: {}", url);

    try {
      String response = restClient.get().uri(url).retrieve().body(String.class);

      if (response == null || response.isBlank()) {
        log.warn("가이드 API 응답이 비어있습니다.");
        throw new CustomException(GuideErrorStatus.EMPTY_RESPONSE);
      }

      return parseThemeResponse(response);

    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error(
          "관광지 키워드 검색 실패: keyword={}, pageNo={}, numOfRows={}, error={}",
          keyword,
          pageNo,
          numOfRows,
          e.getMessage(),
          e);
      throw new CustomException(GuideErrorStatus.API_REQUEST_FAILED);
    }
  }

  @Override
  public AudioStoryListResponse getAudioStoryBasedList(
      String themeId, Integer pageNo, Integer numOfRows) {
    URI url =
        UriComponentsBuilder.fromHttpUrl(audioApiHost + "/storyBasedList")
            .queryParam("serviceKey", serviceKey)
            .queryParam("MobileOS", "ETC")
            .queryParam("MobileApp", "GBJS")
            .queryParam("tid", themeId)
            .queryParam("pageNo", pageNo)
            .queryParam("numOfRows", numOfRows)
            .queryParam("langCode", "ko")
            .queryParam("_type", "json")
            .build(true)
            .toUri();

    log.info("Requesting audio story based list: {}", url);

    try {
      String response = restClient.get().uri(url).retrieve().body(String.class);

      if (response == null || response.isBlank()) {
        log.warn("가이드 API 응답이 비어있습니다.");
        throw new CustomException(GuideErrorStatus.EMPTY_RESPONSE);
      }

      return parseAudioStoryResponse(response);

    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error(
          "오디오 스토리 기본 정보 목록 조회 실패: themeId={}, pageNo={}, numOfRows={}, error={}",
          themeId,
          pageNo,
          numOfRows,
          e.getMessage(),
          e);
      throw new CustomException(GuideErrorStatus.API_REQUEST_FAILED);
    }
  }

  @Override
  public AudioStoryListResponse getAudioStoryLocationBasedList(
      Double longitude, Double latitude, Integer radius, Integer pageNo, Integer numOfRows) {
    URI url =
        UriComponentsBuilder.fromHttpUrl(audioApiHost + "/storyLocationBasedList")
            .queryParam("serviceKey", serviceKey)
            .queryParam("MobileOS", "ETC")
            .queryParam("MobileApp", "GBJS")
            .queryParam("mapX", longitude)
            .queryParam("mapY", latitude)
            .queryParam("radius", radius)
            .queryParam("pageNo", pageNo)
            .queryParam("numOfRows", numOfRows)
            .queryParam("langCode", "ko")
            .queryParam("_type", "json")
            .build(true)
            .toUri();

    log.info("Requesting audio story location based list: {}", url);

    try {
      String response = restClient.get().uri(url).retrieve().body(String.class);

      if (response == null || response.isBlank()) {
        log.warn("가이드 API 응답이 비어있습니다.");
        throw new CustomException(GuideErrorStatus.EMPTY_RESPONSE);
      }

      return parseAudioStoryResponse(response);

    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error(
          "오디오 스토리 위치기반 목록 조회 실패: longitude={}, latitude={}, radius={}, error={}",
          longitude,
          latitude,
          radius,
          e.getMessage(),
          e);
      throw new CustomException(GuideErrorStatus.API_REQUEST_FAILED);
    }
  }

  @Override
  public AudioStoryListResponse getAudioStorySearchList(
      String keyword, Integer pageNo, Integer numOfRows) {
    URI url =
        UriComponentsBuilder.fromHttpUrl(audioApiHost + "/storySearchList")
            .queryParam("serviceKey", serviceKey)
            .queryParam("MobileOS", "ETC")
            .queryParam("MobileApp", "GBJS")
            .queryParam("keyword", URLEncoder.encode(keyword, StandardCharsets.UTF_8))
            .queryParam("pageNo", pageNo)
            .queryParam("numOfRows", numOfRows)
            .queryParam("langCode", "ko")
            .queryParam("_type", "json")
            .build(true)
            .toUri();

    log.info("Requesting audio story search list: {}", url);

    try {
      String response = restClient.get().uri(url).retrieve().body(String.class);

      if (response == null || response.isBlank()) {
        log.warn("가이드 API 응답이 비어있습니다.");
        throw new CustomException(GuideErrorStatus.EMPTY_RESPONSE);
      }

      return parseAudioStoryResponse(response);

    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error(
          "오디오 스토리 키워드 검색 실패: keyword={}, pageNo={}, numOfRows={}, error={}",
          keyword,
          pageNo,
          numOfRows,
          e.getMessage(),
          e);
      throw new CustomException(GuideErrorStatus.API_REQUEST_FAILED);
    }
  }

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

  private String buildAddress(String addr1, String addr2) {
    if (addr1 == null || addr1.isBlank()) {
      return addr2;
    }
    if (addr2 == null || addr2.isBlank()) {
      return addr1;
    }
    return addr1 + " " + addr2;
  }

  private Integer parsePlayTime(String playTime) {
    if (playTime == null || playTime.trim().isEmpty()) {
      return null;
    }
    try {
      return Integer.parseInt(playTime.trim());
    } catch (NumberFormatException e) {
      log.warn("재생 시간 파싱 실패: {}", playTime);
      return null;
    }
  }
}
