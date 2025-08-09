/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.festival.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yfive.gbjs.domain.festival.dto.response.FestivalDetailResponse;
import com.yfive.gbjs.domain.festival.dto.response.FestivalResponse;
import com.yfive.gbjs.global.common.response.ApiResponse;
import com.yfive.gbjs.global.common.response.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "축제", description = "축제 관련 API (공공데이터 관광정보 OpenAPI)")
@RequestMapping("/api/festivals")
public interface FestivalController {

  @GetMapping()
  @Operation(summary = "지역 이름 기반 축제 조회", description = "해당 지역의 현재 날짜부터 6개월 동안 열리는 축제 리스트 반환")
  ResponseEntity<ApiResponse<PageResponse<FestivalResponse>>> getFestivalsByRegion(
      @Parameter(description = "지역", example = "안동시") @RequestParam String region,
      @Parameter(description = "페이지 번호", example = "1") @RequestParam Integer pageNum,
      @Parameter(description = "페이지 크기", example = "3") @RequestParam Integer pageSize);

  @GetMapping("/{id}")
  @Operation(summary = "축제 단일 조회", description = "식별자를 통해 선택한 축제의 정보  반환")
  ResponseEntity<ApiResponse<FestivalDetailResponse>> getFestivalById(
      @Parameter(description = "축제 식별자", example = "2867141") @PathVariable String id);
}
