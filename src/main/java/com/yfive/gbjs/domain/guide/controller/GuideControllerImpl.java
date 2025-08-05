/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yfive.gbjs.domain.guide.dto.response.AudioStoryDetailResponse;
import com.yfive.gbjs.domain.guide.dto.response.AudioStoryListResponse;
import com.yfive.gbjs.domain.guide.dto.response.AudioStorySimpleListResponse;
import com.yfive.gbjs.domain.guide.service.GuideService;
import com.yfive.gbjs.global.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 가이드 컨트롤러 구현체
 *
 * <p>오디오가이드 관련 API 엔드포인트를 제공합니다.
 *
 * @author YFIVE
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class GuideControllerImpl implements GuideController {

  /** 가이드 서비스 */
  private final GuideService guideService;

  /**
   * 오디오 스토리 통합 조회를 처리합니다. 파라미터에 따라 기본정보, 위치기반, 키워드 검색을 수행합니다.
   *
   * @param spotId 관광지 ID (기본정보 조회시)
   * @param longitude 경도 (위치기반 조회시)
   * @param latitude 위도 (위치기반 조회시)
   * @param keyword 검색 키워드 (키워드 검색시)
   * @param pageNo 페이지 번호 (기본값: 1)
   * @param numOfRows 페이지당 결과 수 (기본값: 10)
   * @return 오디오 스토리 목록이 포함된 응답 객체
   */
  @Override
  public ResponseEntity<ApiResponse<AudioStoryListResponse>> getAudioStoryList(
      @RequestParam(required = false) String spotId,
      @RequestParam(required = false) Double longitude,
      @RequestParam(required = false) Double latitude,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") Integer pageNo,
      @RequestParam(defaultValue = "10") Integer numOfRows) {

    AudioStoryListResponse response;

    // 키워드 검색
    if (keyword != null && !keyword.trim().isEmpty()) {
      response = guideService.getAudioStorySearchList(keyword, pageNo, numOfRows);
    }
    // 위치기반 조회
    else if (longitude != null && latitude != null) {
      response =
          guideService.getAudioStoryLocationBasedList(longitude, latitude, pageNo, numOfRows);
    }
    // 기본정보 조회 (spotId 유무와 관계없이)
    else {
      response = guideService.getAudioStoryBasedList(spotId, null, pageNo, numOfRows);
    }

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /**
   * 오디오 스토리 간략 목록을 조회합니다. 파라미터에 따라 기본정보, 위치기반, 키워드 검색을 수행합니다.
   *
   * @param spotId 관광지 ID (기본정보 조회시)
   * @param longitude 경도 (위치기반 조회시)
   * @param latitude 위도 (위치기반 조회시)
   * @param keyword 검색 키워드 (키워드 검색시)
   * @param pageNo 페이지 번호 (기본값: 1)
   * @param numOfRows 페이지당 결과 수 (기본값: 10)
   * @return 오디오 스토리 간략 목록이 포함된 응답 객체
   */
  @Override
  public ResponseEntity<ApiResponse<AudioStorySimpleListResponse>> getAudioStorySimpleList(
      @RequestParam(required = false) String spotId,
      @RequestParam(required = false) Double longitude,
      @RequestParam(required = false) Double latitude,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") Integer pageNo,
      @RequestParam(defaultValue = "10") Integer numOfRows) {

    AudioStorySimpleListResponse response =
        guideService.getAudioStorySimpleList(
            spotId, longitude, latitude, keyword, pageNo, numOfRows);

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /**
   * 특정 관광지의 오디오 스토리 상세 정보를 조회합니다.
   *
   * @param spotId 관광지 ID
   * @return 오디오 스토리 상세 정보가 포함된 응답 객체
   */
  @Override
  public ResponseEntity<ApiResponse<AudioStoryDetailResponse>> getAudioStoryDetail(
      @PathVariable String spotId) {

    AudioStoryDetailResponse response = guideService.getAudioStoryDetail(spotId);

    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
