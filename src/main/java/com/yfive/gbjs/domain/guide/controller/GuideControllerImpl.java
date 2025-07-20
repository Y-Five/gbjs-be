/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yfive.gbjs.domain.guide.dto.response.AudioStoryListResponse;
import com.yfive.gbjs.domain.guide.dto.response.GuideListResponse;
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
   * 관광지 기본 정보 목록을 페이징 조회합니다.
   *
   * @param pageNo 페이지 번호 (기본값: 1)
   * @param numOfRows 페이지당 결과 수 (기본값: 10)
   * @return 관광지 목록이 포함된 응답 객체
   */
  @Override
  public ResponseEntity<ApiResponse<GuideListResponse>> getThemeBasedList(
      @RequestParam(defaultValue = "1") Integer pageNo,
      @RequestParam(defaultValue = "10") Integer numOfRows) {
    GuideListResponse response = guideService.getThemeBasedList(pageNo, numOfRows);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /**
   * 특정 GPS 좌표를 중심으로 반경 내 관광지 목록을 조회합니다.
   *
   * @param longitude 경도 (예: 128.505832)
   * @param latitude 위도 (예: 36.5759985)
   * @param radius 검색 반경(m) (기본값: 1000)
   * @param pageNo 페이지 번호 (기본값: 1)
   * @param numOfRows 페이지당 결과 수 (기본값: 10)
   * @return 위치 기반 관광지 목록이 포함된 응답 객체
   */
  @Override
  public ResponseEntity<ApiResponse<GuideListResponse>> getThemeLocationBasedList(
      @RequestParam Double longitude,
      @RequestParam Double latitude,
      @RequestParam(defaultValue = "1000") Integer radius,
      @RequestParam(defaultValue = "1") Integer pageNo,
      @RequestParam(defaultValue = "10") Integer numOfRows) {
    GuideListResponse response =
        guideService.getThemeLocationBasedList(longitude, latitude, radius, pageNo, numOfRows);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /**
   * 키워드로 관광지를 검색합니다.
   *
   * @param keyword 검색 키워드 (예: 경복궁)
   * @param pageNo 페이지 번호 (기본값: 1)
   * @param numOfRows 페이지당 결과 수 (기본값: 10)
   * @return 키워드 검색 결과 관광지 목록이 포함된 응답 객체
   */
  @Override
  public ResponseEntity<ApiResponse<GuideListResponse>> getThemeSearchList(
      @RequestParam String keyword,
      @RequestParam(defaultValue = "1") Integer pageNo,
      @RequestParam(defaultValue = "10") Integer numOfRows) {
    GuideListResponse response = guideService.getThemeSearchList(keyword, pageNo, numOfRows);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /**
   * 관광지 ID를 기준으로 관련 오디오 스토리 정보를 조회합니다.
   *
   * @param themeId 관광지 ID (예: TH00001)
   * @param pageNo 페이지 번호 (기본값: 1)
   * @param numOfRows 페이지당 결과 수 (기본값: 10)
   * @return 오디오 스토리 목록이 포함된 응답 객체
   */
  @Override
  public ResponseEntity<ApiResponse<AudioStoryListResponse>> getAudioStoryBasedList(
      @PathVariable String themeId,
      @RequestParam(defaultValue = "1") Integer pageNo,
      @RequestParam(defaultValue = "10") Integer numOfRows) {
    AudioStoryListResponse response =
        guideService.getAudioStoryBasedList(themeId, pageNo, numOfRows);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /**
   * 좌표를 기준으로 주변 오디오 스토리 목록을 조회합니다.
   *
   * @param longitude 경도 (예: 128.505832)
   * @param latitude 위도 (예: 36.5759985)
   * @param radius 검색 반경(m) (기본값: 1000)
   * @param pageNo 페이지 번호 (기본값: 1)
   * @param numOfRows 페이지당 결과 수 (기본값: 10)
   * @return 위치 기반 오디오 스토리 목록이 포함된 응답 객체
   */
  @Override
  public ResponseEntity<ApiResponse<AudioStoryListResponse>> getAudioStoryLocationBasedList(
      @RequestParam Double longitude,
      @RequestParam Double latitude,
      @RequestParam(defaultValue = "1000") Integer radius,
      @RequestParam(defaultValue = "1") Integer pageNo,
      @RequestParam(defaultValue = "10") Integer numOfRows) {
    AudioStoryListResponse response =
        guideService.getAudioStoryLocationBasedList(longitude, latitude, radius, pageNo, numOfRows);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /**
   * 키워드로 오디오 스토리를 검색합니다.
   *
   * @param keyword 검색 키워드
   * @param pageNo 페이지 번호 (기본값: 1)
   * @param numOfRows 페이지당 결과 수 (기본값: 10)
   * @return 키워드 검색 결과 오디오 스토리 목록이 포함된 응답 객체
   */
  @Override
  public ResponseEntity<ApiResponse<AudioStoryListResponse>> getAudioStorySearchList(
      @RequestParam String keyword,
      @RequestParam(defaultValue = "1") Integer pageNo,
      @RequestParam(defaultValue = "10") Integer numOfRows) {
    AudioStoryListResponse response =
        guideService.getAudioStorySearchList(keyword, pageNo, numOfRows);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
