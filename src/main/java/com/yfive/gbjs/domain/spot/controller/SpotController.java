/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.spot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yfive.gbjs.domain.spot.dto.response.SpotResponse;
import com.yfive.gbjs.global.common.response.ApiResponse;
import com.yfive.gbjs.global.common.response.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "관광지", description = "관광지 관련 API (공공데이터 관광정보 OpenAPI)")
@RequestMapping("/api/spots")
public interface SpotController {

  @GetMapping
  @Operation(summary = "관광지 검색", description = "검색 키워드를 기반으로 관광지 리스트 반환")
  ResponseEntity<ApiResponse<PageResponse<SpotResponse>>> getSpotsByKeyword(
      @Parameter(description = "검색 키워드", example = "경주") @RequestParam String keyword,
      @Parameter(description = "페이지 번호", example = "0") @RequestParam Integer pageNum,
      @Parameter(description = "페이지 크기", example = "4") @RequestParam Integer pageSize,
      @Parameter(description = "정렬 기준", example = "거리순") @RequestParam String sortBy,
      @Parameter(description = "경도", example = "128.505832") @RequestParam Double longitude,
      @Parameter(description = "위도", example = "36.5759985") @RequestParam Double latitude);

  @GetMapping("{id}")
  @Operation(summary = "관광지 단일 조회", description = "관광지 식별자를 통한 단일 조회")
  ResponseEntity<ApiResponse<SpotResponse>> getSpotByContentId(
      @Parameter(description = "관광지 식별자", example = "126207") @PathVariable("id") Long contentId,
      @Parameter(description = "경도", example = "128.505832") @RequestParam Double longitude,
      @Parameter(description = "위도", example = "36.5759985") @RequestParam Double latitude);
}
