/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.service;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import com.yfive.gbjs.domain.guide.dto.response.CoordinateValidationResponse;
import com.yfive.gbjs.domain.guide.entity.AudioGuide;
import com.yfive.gbjs.domain.guide.repository.AudioGuideRepository;
import com.yfive.gbjs.domain.guide.util.GeoJsonBoundaryChecker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuideServiceImpl implements GuideService {

  private final RestClient restClient;
  private final ObjectMapper objectMapper;
  private final AudioGuideRepository audioGuideRepository;
  private final AudioGuideConverter audioGuideConverter;
  private final GeoJsonBoundaryChecker geoJsonBoundaryChecker;

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
    if (playTime == null || playTime.trim().isEmpty()) {
      return null;
    }
    try {
      return Integer.parseInt(playTime.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * 주어진 좌표가 경상북도 지역에 속하는지 확인합니다. GeoJSON 파일이 있으면 정확한 경계 검사를 수행하고, 없으면 기존 좌표 범위 방식으로 폴백합니다.
   *
   * @param latitude 위도
   * @param longitude 경도
   * @return 경북 지역 포함 여부
   */
  private boolean isInGyeongbukRegion(double latitude, double longitude) {
    return geoJsonBoundaryChecker.isInGyeongbukRegion(latitude, longitude);
  }

  /**
   * 초기 데이터 로드 - storyBasedList API를 사용하여 전체 오디오 가이드 데이터를 가져옵니다.
   *
   * @return 저장된 데이터 개수
   */
  @Transactional
  public int loadInitialGyeongbukAudioGuides() {
    log.info("=== 초기 데이터 로드 시작 ===");
    int totalSavedCount = 0;
    int pageNo = 1;
    boolean hasMoreData = true;

    String currentSyncTime =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

    while (hasMoreData) {
      Map<String, Object> params = new java.util.HashMap<>();
      params.put("pageNo", pageNo);
      params.put("numOfRows", 1000);

      URI url = buildUri("/storyBasedList", params);
      log.info("API 호출 URL: {}", url);

      try {
        String response = restClient.get().uri(url).retrieve().body(String.class);
        log.info("API 응답 받음 (길이: {})", response != null ? response.length() : 0);
        if (response == null || response.isBlank()) {
          break;
        }

        JsonNode root = objectMapper.readTree(response);
        JsonNode body = root.path("response").path("body");

        if (body.isMissingNode()) {
          break;
        }

        int totalCount = body.path("totalCount").asInt(0);
        int numOfRows = body.path("numOfRows").asInt(0);

        JsonNode items = body.path("items").path("item");
        log.info("총 개수: {}, 현재 페이지 아이템 수: {}", totalCount, items.isArray() ? items.size() : 0);

        if (items.isArray() && items.size() > 0) {
          log.info("아이템 처리 시작...");
          for (JsonNode item : items) {
            String mapX = item.path("mapX").asText();
            String mapY = item.path("mapY").asText();

            // 경상북도 지역 좌표 범위 확인
            if (mapX != null && mapY != null && !mapX.isEmpty() && !mapY.isEmpty()) {
              try {
                double longitude = Double.parseDouble(mapX);
                double latitude = Double.parseDouble(mapY);

                // 경북 지역 확인 (대구, 충주 제외)
                if (isInGyeongbukRegion(latitude, longitude)) {

                  String tid = item.path("tid").asText();
                  String title = item.path("title").asText();

                  // storyBasedList API에서 직접 모든 정보를 가져옴
                  AudioGuide audioGuide =
                      AudioGuide.builder()
                          .tid(tid)
                          .tlid(item.path("tlid").asText())
                          .title(title)
                          .longitude(mapX)
                          .latitude(mapY)
                          .langCode(item.path("langCode").asText())
                          .imageUrl(item.path("imageUrl").asText())
                          .syncStatus("A") // 초기 로드는 모두 신규
                          .apiCreatedTime(item.path("createdtime").asText())
                          .apiModifiedTime(item.path("modifiedtime").asText())
                          .lastSyncedAt(currentSyncTime)
                          // 오디오 정보 직접 매핑
                          .audioGuideId(item.path("stid").asText())
                          .stlid(item.path("stlid").asText())
                          .audioTitle(item.path("audioTitle").asText())
                          .script(item.path("script").asText())
                          .playTime(parsePlayTime(item.path("playTime").asText()))
                          .audioUrl(item.path("audioUrl").asText())
                          .build();

                  audioGuideRepository.save(audioGuide);
                  totalSavedCount++;
                  log.debug("저장 완료: {} (총 {}개)", title, totalSavedCount);
                }
              } catch (NumberFormatException e) {
                // 좌표 파싱 실패 시 무시
              }
            }
          }

          // 다음 페이지 확인
          if (pageNo * numOfRows >= totalCount || items.size() < numOfRows) {
            hasMoreData = false;
          } else {
            pageNo++;
          }
        } else {
          hasMoreData = false;
        }
      } catch (Exception e) {
        log.error("오류 발생: {}", e.getMessage(), e);
        break;
      }
    }

    log.info("=== 초기 데이터 로드 완료. 총 저장: {}개 ===", totalSavedCount);
    return totalSavedCount;
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public int syncGyeongbukAudioStories() {

    // DB에 데이터가 있는지 확인
    long dbRecordCount = audioGuideRepository.count();

    // DB가 비어있으면 초기 데이터 로드
    if (dbRecordCount == 0) {
      return loadInitialGyeongbukAudioGuides();
    }

    // 동기화 전 경북 외부 데이터 정리 (기존 메서드 재사용)
    int cleanedCount = deleteOutsideGyeongbukData();
    if (cleanedCount > 0) {
      log.info("동기화 전 {}개의 경북 외부 데이터 정리 완료", cleanedCount);
    }

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

                // 경북 지역 확인 (대구, 충주 제외)
                if (isInGyeongbukRegion(latitude, longitude)) {

                  pageGyeongbukCount++;
                  String tid = item.path("tid").asText();
                  String syncStatus = item.path("syncStatus").asText();

                  // syncStatus에 따른 처리
                  if ("D".equals(syncStatus)) {
                    // 삭제 처리
                    audioGuideRepository.deleteByTid(tid);
                    pageDeletedCount++;
                  } else {
                    // 신규(A) 또는 수정(U) 처리
                    Optional<AudioGuide> existingGuide = audioGuideRepository.findByTid(tid);

                    // syncList API
                    AudioGuide audioGuide =
                        AudioGuide.builder()
                            .tid(tid)
                            .tlid(item.path("tlid").asText())
                            .title(item.path("title").asText())
                            .longitude(mapX)
                            .latitude(mapY)
                            .langCode(item.path("langCode").asText())
                            .imageUrl(item.path("imageUrl").asText())
                            .syncStatus(syncStatus)
                            .apiCreatedTime(item.path("createdtime").asText())
                            .apiModifiedTime(item.path("modifiedtime").asText())
                            .lastSyncedAt(currentSyncTime)
                            .audioGuideId(item.path("stid").asText())
                            .stlid(item.path("stlid").asText())
                            .audioTitle(item.path("audioTitle").asText())
                            .script(item.path("script").asText())
                            .playTime(parsePlayTime(item.path("playTime").asText()))
                            .audioUrl(item.path("audioUrl").asText())
                            .build();

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

    // 정리된 개수 + 신규 개수 + 업데이트 개수를 모두 포함
    int totalProcessed = cleanedCount + totalNewCount + totalUpdatedCount;
    log.info(
        "=== 동기화 완료: 정리 {}개, 신규 {}개, 업데이트 {}개, 총 {}개 처리 ===",
        cleanedCount,
        totalNewCount,
        totalUpdatedCount,
        totalProcessed);

    return totalProcessed;
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

  /** {@inheritDoc} */
  @Override
  public CoordinateValidationResponse validateStoredCoordinates() {
    log.info("=== DB 저장 데이터 좌표 검증 시작 ===");

    List<AudioGuide> allGuides = audioGuideRepository.findAll();
    int total = allGuides.size();
    int insideCount = 0;
    int outsideCount = 0;
    List<CoordinateValidationResponse.OutsideData> outsideList = new ArrayList<>();

    for (AudioGuide guide : allGuides) {
      String mapX = guide.getLongitude();
      String mapY = guide.getLatitude();

      if (mapX != null && mapY != null && !mapX.isEmpty() && !mapY.isEmpty()) {
        try {
          double longitude = Double.parseDouble(mapX);
          double latitude = Double.parseDouble(mapY);

          if (geoJsonBoundaryChecker.isInGyeongbukRegion(latitude, longitude)) {
            insideCount++;
            log.debug("경북 내부: {} ({}, {})", guide.getTitle(), latitude, longitude);
          } else {
            outsideCount++;
            log.info("경북 외부: {} (위도: {}, 경도: {})", guide.getTitle(), latitude, longitude);

            // 경북 외부 데이터를 리스트에 추가 (최대 100개)
            if (outsideList.size() < 100) {
              outsideList.add(
                  CoordinateValidationResponse.OutsideData.builder()
                      .title(guide.getTitle())
                      .latitude(latitude)
                      .longitude(longitude)
                      .build());
            }
          }
        } catch (NumberFormatException e) {
          log.warn("좌표 파싱 실패: {} - ({}, {})", guide.getTitle(), mapX, mapY);
          outsideCount++;
        }
      } else {
        log.warn("좌표 누락: {}", guide.getTitle());
        outsideCount++;
      }
    }

    double percentage = total > 0 ? (insideCount * 100.0 / total) : 0;

    log.info(
        "=== 검증 완료: 전체 {}개, 경북 내부 {}개 ({:.2f}%), 경북 외부 {}개 ===",
        total, insideCount, percentage, outsideCount);

    return CoordinateValidationResponse.builder()
        .total(total)
        .insideGyeongbuk(insideCount)
        .outsideGyeongbuk(outsideCount)
        .insidePercentage(percentage)
        .outsideDataList(outsideList)
        .build();
  }

  /** {@inheritDoc} */
  @Override
  public boolean testCoordinate(double latitude, double longitude) {
    log.info("좌표 테스트: 위도={}, 경도={}", latitude, longitude);
    boolean result = geoJsonBoundaryChecker.isInGyeongbukRegion(latitude, longitude);
    log.info("결과: 경북 {}", result ? "내부" : "외부");
    return result;
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public int deleteOutsideGyeongbukData() {
    log.info("=== 경북 외부 데이터 삭제 시작 ===");

    List<AudioGuide> allGuides = audioGuideRepository.findAll();
    List<AudioGuide> toDelete = new ArrayList<>();

    for (AudioGuide guide : allGuides) {
      String mapX = guide.getLongitude();
      String mapY = guide.getLatitude();

      if (mapX != null && mapY != null && !mapX.isEmpty() && !mapY.isEmpty()) {
        try {
          double longitude = Double.parseDouble(mapX);
          double latitude = Double.parseDouble(mapY);

          // 경북 외부 데이터를 삭제 대상에 추가
          if (!geoJsonBoundaryChecker.isInGyeongbukRegion(latitude, longitude)) {
            toDelete.add(guide);
            log.info("삭제 대상: {} (위도: {}, 경도: {})", guide.getTitle(), latitude, longitude);
          }
        } catch (NumberFormatException e) {
          // 좌표 파싱 실패한 데이터도 삭제
          toDelete.add(guide);
          log.warn("좌표 파싱 실패, 삭제 대상: {}", guide.getTitle());
        }
      } else {
        // 좌표가 없는 데이터도 삭제
        toDelete.add(guide);
        log.warn("좌표 누락, 삭제 대상: {}", guide.getTitle());
      }
    }

    // 배치 삭제
    if (!toDelete.isEmpty()) {
      audioGuideRepository.deleteAll(toDelete);
      log.info("=== {}개의 경북 외부 데이터 삭제 완료 ===", toDelete.size());
    } else {
      log.info("=== 삭제할 경북 외부 데이터가 없습니다 ===");
    }

    return toDelete.size();
  }
}
