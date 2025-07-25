/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.festival.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yfive.gbjs.domain.festival.dto.response.FestivalListResponse;
import com.yfive.gbjs.global.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "축제", description = "축제 관련 API (공공데이터 관광정보 OpenAPI)")
@RequestMapping("/api/festivals")
public interface FestivalController {

  @GetMapping
  @Operation(summary = "지역 이름 기반 축제 조회", description = "지역 이름을 기반으로 해당 지역에 대한 축제 리스트 반환")
  ResponseEntity<ApiResponse<FestivalListResponse>> getFestivalsByRegion(
      @Parameter(description = "지역", example = "안동시") @RequestParam String region,
      @Parameter(description = "첫 번째 인덱스 값", example = "4") @RequestParam Integer startIndex,
      @Parameter(description = "페이지 크기", example = "3") @RequestParam Integer pageSize);
}
