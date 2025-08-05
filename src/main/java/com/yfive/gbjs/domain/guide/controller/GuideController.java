/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yfive.gbjs.domain.guide.dto.response.AudioStoryDetailResponse;
import com.yfive.gbjs.domain.guide.dto.response.AudioStoryListResponse;
import com.yfive.gbjs.domain.guide.dto.response.AudioStorySimpleListResponse;
import com.yfive.gbjs.global.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "가이드", description = "오디오가이드 API (공공데이터 오디 OpenAPI)")
@RequestMapping("/api/guides")
public interface GuideController {

  @GetMapping("/audio-stories")
  @Operation(summary = "오디오 스토리 통합 조회", description = "기본정보, 위치기반, 키워드 검색을 하나의 API로 통합 조회")
  public ResponseEntity<ApiResponse<AudioStoryListResponse>> getAudioStoryList(
      @Parameter(description = "관광지 ID(필요시 기본순)", example = "TH00001")
          @RequestParam(required = false)
          String spotId,
      @Parameter(description = "경도 (위치기반 조회시)", example = "128.505832")
          @RequestParam(required = false)
          Double longitude,
      @Parameter(description = "위도 (위치기반 조회시)", example = "36.5759985")
          @RequestParam(required = false)
          Double latitude,
      @Parameter(description = "검색 키워드 (키워드 검색시)", example = "왕") @RequestParam(required = false)
          String keyword,
      @Parameter(description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1")
          Integer pageNo,
      @Parameter(description = "페이지당 결과 수", example = "10") @RequestParam(defaultValue = "10")
          Integer numOfRows);

  @GetMapping("/audio-stories/simple")
  @Operation(summary = "오디오 관광지 간략 목록 조회", description = "오디오 스토리의 기본 정보만을 포함한 간략한 목록을 조회")
  public ResponseEntity<ApiResponse<AudioStorySimpleListResponse>> getAudioStorySimpleList(
      @Parameter(description = "관광지 ID(필요시 기본순)", example = "TH00001")
          @RequestParam(required = false)
          String spotId,
      @Parameter(description = "경도 (위치기반 조회시)", example = "128.505832")
          @RequestParam(required = false)
          Double longitude,
      @Parameter(description = "위도 (위치기반 조회시)", example = "36.5759985")
          @RequestParam(required = false)
          Double latitude,
      @Parameter(description = "검색 키워드 (키워드 검색시)", example = "왕") @RequestParam(required = false)
          String keyword,
      @Parameter(description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1")
          Integer pageNo,
      @Parameter(description = "페이지당 결과 수", example = "10") @RequestParam(defaultValue = "10")
          Integer numOfRows);

  @GetMapping("/audio-stories/detail/{spotId}")
  @Operation(summary = "오디오 스토리 상세 정보 조회", description = "특정 관광지의 모든 오디오 스토리 상세 정보를 조회")
  public ResponseEntity<ApiResponse<AudioStoryDetailResponse>> getAudioStoryDetail(
      @Parameter(description = "관광지 ID", example = "TH00001", required = true) @PathVariable
          String spotId);
}
