/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.service;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfive.gbjs.domain.guide.converter.AudioGuideConverter;
import com.yfive.gbjs.domain.guide.dto.response.AudioDetailResponse;
import com.yfive.gbjs.domain.guide.entity.AudioGuide;
import com.yfive.gbjs.domain.guide.repository.AudioGuideRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GuideServiceImpl implements GuideService {

  private final RestClient restClient;
  private final ObjectMapper objectMapper;
  private final AudioGuideRepository audioGuideRepository;
  private final AudioGuideConverter audioGuideConverter;

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
      return null;
    }
  }

  /**
   * 관광지의 오디오 상세 정보를 가져옵니다.
   *
   * @param spotId 관광지 ID
   * @return 오디오 상세 정보
   */
  private JsonNode fetchAudioDetail(String spotId) {
    Map<String, Object> params = new java.util.HashMap<>();
    params.put("tid", spotId);
    params.put("pageNo", 1);
    params.put("numOfRows", 10);

    URI url = buildUri("/storyBasedList", params);

    try {
      String response = restClient.get().uri(url).retrieve().body(String.class);
      if (response == null || response.isBlank()) {
        return null;
      }

      JsonNode root = objectMapper.readTree(response);
      JsonNode items = root.path("response").path("body").path("items").path("item");

      if (items.isArray() && items.size() > 0) {
        return items.get(0);
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public int syncGyeongbukAudioStories() {

    // 마지막 동기화 시간 조회
    Optional<String> lastModifiedTime = audioGuideRepository.findLatestModifiedTime();
    String modifiedTimeParam = lastModifiedTime.orElse(null);

    if (modifiedTimeParam != null) {
    } else {
    }

    String currentSyncTime =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

    int totalNewCount = 0;
    int totalUpdatedCount = 0;
    int totalDeletedCount = 0;
    int pageNo = 1;
    boolean hasMoreData = true;

    // 페이지네이션을 통해 모든 데이터 가져오기
    while (hasMoreData) {
      Map<String, Object> params = new java.util.HashMap<>();
      params.put("pageNo", pageNo);
      params.put("numOfRows", 1000);

      // 증분 동기화를 위한 파라미터 설정
      if (modifiedTimeParam != null) {
        params.put("modifiedtime", modifiedTimeParam.substring(0, 8)); // yyyyMMdd 형식
      }

      URI url = buildUri("/storyBasedSyncList", params);

      try {
        String response = restClient.get().uri(url).retrieve().body(String.class);
        if (response == null || response.isBlank()) {
          break;
        }

        JsonNode root = objectMapper.readTree(response);
        JsonNode body = root.path("response").path("body");

        if (body.isMissingNode()) {
          break;
        }

        // 전체 개수와 현재 페이지 정보 확인
        int totalCount = body.path("totalCount").asInt(0);
        int numOfRows = body.path("numOfRows").asInt(0);

        JsonNode items = body.path("items").path("item");
        int pageGyeongbukCount = 0;
        int pageNewCount = 0;
        int pageUpdatedCount = 0;
        int pageDeletedCount = 0;

        if (items.isArray() && items.size() > 0) {
          for (JsonNode item : items) {
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

                  pageGyeongbukCount++;
                  String spotId = item.path("tid").asText();
                  String syncStatus = item.path("syncStatus").asText();

                  // syncStatus에 따른 처리
                  if ("D".equals(syncStatus)) {
                    // 삭제 처리
                    audioGuideRepository.deleteBySpotId(spotId);
                    pageDeletedCount++;
                  } else {
                    // 신규(A) 또는 수정(U) 처리
                    Optional<AudioGuide> existingGuide = audioGuideRepository.findBySpotId(spotId);

                    // 상세 정보 가져오기 (syncList API에는 상세 정보가 없음)
                    JsonNode detailInfo = fetchAudioDetail(spotId);

                    AudioGuide.AudioGuideBuilder builder =
                        AudioGuide.builder()
                            .spotId(spotId)
                            .tlid(item.path("tlid").asText())
                            .title(item.path("title").asText())
                            .longitude(mapX)
                            .latitude(mapY)
                            .langCode(item.path("langCode").asText())
                            .imageUrl(item.path("imageUrl").asText())
                            .syncStatus(syncStatus)
                            .apiCreatedTime(item.path("createdtime").asText())
                            .apiModifiedTime(item.path("modifiedtime").asText())
                            .lastSyncedAt(currentSyncTime);

                    // 상세 정보가 있으면 추가
                    if (detailInfo != null) {
                      builder
                          .audioGuideId(detailInfo.path("stid").asText())
                          .stlid(detailInfo.path("stlid").asText())
                          .audioTitle(detailInfo.path("audioTitle").asText())
                          .script(detailInfo.path("script").asText())
                          .playTime(parsePlayTime(detailInfo.path("playTime").asText()))
                          .audioUrl(detailInfo.path("audioUrl").asText());
                    }

                    AudioGuide audioGuide = builder.build();

                    if (existingGuide.isPresent()) {
                      // 기존 데이터 업데이트
                      AudioGuide existing = existingGuide.get();
                      existing.updateFromSync(audioGuide);
                      audioGuideRepository.save(existing);
                      pageUpdatedCount++;
                    } else {
                      // 신규 데이터 저장
                      audioGuideRepository.save(audioGuide);
                      pageNewCount++;
                    }
                  }
                }
              } catch (NumberFormatException e) {
              }
            }
          }

          totalNewCount += pageNewCount;
          totalUpdatedCount += pageUpdatedCount;
          totalDeletedCount += pageDeletedCount;

          // 다음 페이지가 있는지 확인
          if (pageNo * numOfRows >= totalCount || items.size() < numOfRows) {
            hasMoreData = false;
          } else {
            pageNo++;
          }
        } else {
          // 더 이상 데이터가 없음
          hasMoreData = false;
        }

      } catch (Exception e) {
        break;
      }
    }

    return totalNewCount + totalUpdatedCount;
  }

  /** {@inheritDoc} */
  @Override
  public List<AudioDetailResponse> searchAudioGuideByTitle(String title) {
    List<AudioGuide> audioGuides = audioGuideRepository.findByTitle(title);
    if (audioGuides.isEmpty()) {
      throw new com.yfive.gbjs.global.error.exception.CustomException(
          com.yfive.gbjs.domain.guide.exception.GuideErrorStatus.AUDIO_GUIDE_NOT_FOUND);
    }
    return audioGuideConverter.toAudioDetailResponseList(audioGuides);
  }

  /** {@inheritDoc} */
  @Override
  public List<AudioDetailResponse> searchAudioGuideByTitleLike(String title) {
    List<AudioGuide> audioGuides = audioGuideRepository.findByTitleLike(title);
    if (audioGuides.isEmpty()) {
      throw new com.yfive.gbjs.global.error.exception.CustomException(
          com.yfive.gbjs.domain.guide.exception.GuideErrorStatus.AUDIO_GUIDE_NOT_FOUND);
    }
    return audioGuideConverter.toAudioDetailResponseList(audioGuides);
  }
}
