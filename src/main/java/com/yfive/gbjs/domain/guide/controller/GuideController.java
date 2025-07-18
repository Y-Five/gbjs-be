/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yfive.gbjs.domain.guide.dto.response.AudioStoryListResponse;
import com.yfive.gbjs.domain.guide.dto.response.GuideListResponse;
import com.yfive.gbjs.domain.guide.service.GuideService;
import com.yfive.gbjs.global.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/guides")
@Tag(name = "가이드", description = "오디오가이드 API (공공데이터 오디 OpenAPI)")
public class GuideController {

  private final GuideService guideService;

  @Operation(summary = "관광지 기본 정보 목록 조회", description = "관광지 전체 목록을 페이징 조회")
  @GetMapping
  public ResponseEntity<ApiResponse<GuideListResponse>> getThemeBasedList(
      @Parameter(description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1")
          Integer pageNo,
      @Parameter(description = "페이지당 결과 수", example = "10") @RequestParam(defaultValue = "10")
          Integer numOfRows) {
    GuideListResponse response = guideService.getThemeBasedList(pageNo, numOfRows);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "관광지 위치기반 정보 목록 조회", description = "특정 GPS 좌표를 중심으로 반경 내 관광지 목록 조회")
  @GetMapping("/location")
  public ResponseEntity<ApiResponse<GuideListResponse>> getThemeLocationBasedList(
      @Parameter(description = "경도", example = "128.505832") @RequestParam Double longitude,
      @Parameter(description = "위도", example = "36.5759985") @RequestParam Double latitude,
      @Parameter(description = "검색 반경(m)", example = "1000") @RequestParam(defaultValue = "1000")
          Integer radius,
      @Parameter(description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1")
          Integer pageNo,
      @Parameter(description = "페이지당 결과 수", example = "10") @RequestParam(defaultValue = "10")
          Integer numOfRows) {
    GuideListResponse response =
        guideService.getThemeLocationBasedList(longitude, latitude, radius, pageNo, numOfRows);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "관광지 키워드 검색 목록 조회", description = "키워드로 관광지를 검색")
  @GetMapping("/search")
  public ResponseEntity<ApiResponse<GuideListResponse>> getThemeSearchList(
      @Parameter(description = "검색 키워드", example = "경복궁") @RequestParam String keyword,
      @Parameter(description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1")
          Integer pageNo,
      @Parameter(description = "페이지당 결과 수", example = "10") @RequestParam(defaultValue = "10")
          Integer numOfRows) {
    GuideListResponse response = guideService.getThemeSearchList(keyword, pageNo, numOfRows);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "오디오 스토리 기본 정보 목록 조회", description = "관광지 ID 기준으로 관련 오디오 스토리 정보 조회")
  @GetMapping("/audio-stories/{themeId}")
  public ResponseEntity<ApiResponse<AudioStoryListResponse>> getAudioStoryBasedList(
      @Parameter(description = "관광지 ID", example = "TH00001") @PathVariable String themeId,
      @Parameter(description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1")
          Integer pageNo,
      @Parameter(description = "페이지당 결과 수", example = "10") @RequestParam(defaultValue = "10")
          Integer numOfRows) {
    AudioStoryListResponse response =
        guideService.getAudioStoryBasedList(themeId, pageNo, numOfRows);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "오디오 스토리 위치기반 정보 목록 조회", description = "좌표 기반 주변 오디오 스토리 목록 조회")
  @GetMapping("/audio-stories/location")
  public ResponseEntity<ApiResponse<AudioStoryListResponse>> getAudioStoryLocationBasedList(
      @Parameter(description = "경도", example = "128.505832") @RequestParam Double longitude,
      @Parameter(description = "위도", example = "36.5759985") @RequestParam Double latitude,
      @Parameter(description = "검색 반경(m)", example = "1000") @RequestParam(defaultValue = "1000")
          Integer radius,
      @Parameter(description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1")
          Integer pageNo,
      @Parameter(description = "페이지당 결과 수", example = "10") @RequestParam(defaultValue = "10")
          Integer numOfRows) {
    AudioStoryListResponse response =
        guideService.getAudioStoryLocationBasedList(longitude, latitude, radius, pageNo, numOfRows);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "오디오 스토리 키워드 검색 목록 조회", description = "키워드로 오디오 스토리 검색")
  @GetMapping("/audio-stories/search")
  public ResponseEntity<ApiResponse<AudioStoryListResponse>> getAudioStorySearchList(
      @Parameter(description = "검색 키워드", example = "왕") @RequestParam String keyword,
      @Parameter(description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1")
          Integer pageNo,
      @Parameter(description = "페이지당 결과 수", example = "10") @RequestParam(defaultValue = "10")
          Integer numOfRows) {
    AudioStoryListResponse response =
        guideService.getAudioStorySearchList(keyword, pageNo, numOfRows);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
