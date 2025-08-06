/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yfive.gbjs.domain.guide.dto.response.AudioDetailResponse;
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
}
