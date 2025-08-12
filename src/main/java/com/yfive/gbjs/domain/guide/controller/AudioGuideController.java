/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yfive.gbjs.domain.guide.dto.response.AudioDetailResponse;
import com.yfive.gbjs.domain.guide.dto.response.CoordinateValidationResponse;
import com.yfive.gbjs.domain.guide.service.GuideService;
import com.yfive.gbjs.global.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "AudioGuide", description = "오디오 가이드 API")
@RestController
@RequestMapping("/api/audio-guide")
@RequiredArgsConstructor
public class AudioGuideController {

  private final GuideService guideService;

  @Operation(summary = "관광지명 정확히 검색", description = "관광지명이 정확히 일치하는 오디오 가이드를 조회합니다.")
  @GetMapping("/title")
  public ResponseEntity<ApiResponse<List<AudioDetailResponse>>> searchExact(
      @Parameter(description = "검색 관광지명", required = true, example = "불국사") @RequestParam
          String title) {
    List<AudioDetailResponse> results = guideService.searchAudioGuideByTitle(title);
    return ResponseEntity.ok(ApiResponse.success(results));
  }

  @Operation(summary = "관광지명 포함 검색", description = "관광지명이 포함된 오디오 가이드를 조회합니다.")
  @GetMapping("/title-like")
  public ResponseEntity<ApiResponse<List<AudioDetailResponse>>> searchKeyword(
      @Parameter(description = "검색 관광지명", required = true, example = "불국사") @RequestParam
          String title) {
    List<AudioDetailResponse> results = guideService.searchAudioGuideByTitleLike(title);
    return ResponseEntity.ok(ApiResponse.success(results));
  }

  @Operation(
      summary = "경북 오디오 가이드 동기화",
      description = "경북 지역 오디오 가이드 데이터를 동기화합니다. GeoJSON 기반 필터링을 테스트할 수 있습니다.")
  @GetMapping("/sync")
  public ResponseEntity<ApiResponse<String>> syncGyeongbukAudioGuides() {
    int syncCount = guideService.syncGyeongbukAudioStories();
    String message = String.format("동기화 완료: %d개의 데이터가 처리되었습니다.", syncCount);
    return ResponseEntity.ok(ApiResponse.success(message));
  }

  @Operation(
      summary = "저장된 데이터 좌표 검증",
      description = "DB에 저장된 모든 오디오 가이드 데이터의 좌표를 GeoJSON 기반으로 재검증합니다.")
  @GetMapping("/validate-coordinates")
  public ResponseEntity<ApiResponse<CoordinateValidationResponse>> validateStoredCoordinates() {
    CoordinateValidationResponse result = guideService.validateStoredCoordinates();
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  @Operation(summary = "특정 좌표 테스트", description = "특정 좌표가 경북 지역에 속하는지 테스트합니다.")
  @GetMapping("/test-coordinate")
  public ResponseEntity<ApiResponse<String>> testCoordinate(
      @Parameter(description = "위도", required = true, example = "36.0") @RequestParam
          double latitude,
      @Parameter(description = "경도", required = true, example = "129.0") @RequestParam
          double longitude) {
    boolean isInside = guideService.testCoordinate(latitude, longitude);
    String message =
        String.format(
            "좌표 (위도: %.4f, 경도: %.4f) - 경북 %s", latitude, longitude, isInside ? "내부" : "외부");
    return ResponseEntity.ok(ApiResponse.success(message));
  }

  @Operation(summary = "경북 외부 데이터 삭제", description = "경북 지역 외부에 있는 모든 오디오 가이드 데이터를 삭제합니다.")
  @DeleteMapping("/clean-outside")
  public ResponseEntity<ApiResponse<String>> deleteOutsideData() {
    int deletedCount = guideService.deleteOutsideGyeongbukData();
    String message = String.format("%d개의 경북 외부 데이터가 삭제되었습니다.", deletedCount);
    return ResponseEntity.ok(ApiResponse.success(message));
  }
}
