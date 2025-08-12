/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yfive.gbjs.domain.seal.dto.response.SealProductResponse;
import com.yfive.gbjs.domain.seal.dto.response.SealResponse;
import com.yfive.gbjs.domain.seal.dto.response.UserSealResponse;
import com.yfive.gbjs.domain.seal.entity.SortBy;
import com.yfive.gbjs.global.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/** 띠부씰 컨트롤러 인터페이스 띠부씰 관련 API 엔드포인트를 정의 */
@Tag(name = "띠부씰", description = "띠부씰 관련 API")
@RequestMapping("/api/seals")
public interface SealController {

  @GetMapping
  @Operation(summary = "전체 띠부씰 조회", description = "모든 띠부씰 목록을 조회합니다.")
  ResponseEntity<ApiResponse<SealResponse.SealListDTO>> getAllSeals(
      @RequestParam(required = false, defaultValue = "NUMBER")
          @Parameter(description = "정렬 옵션 (NUMBER: 번호순, RARITY: 희귀도순, LOCATION: 지역순)")
          SortBy sortBy);

  @GetMapping("/user")
  @Operation(summary = "회원 띠부씰 조회", description = "로그인된 회원의 띠부씰 수집 현황을 조회합니다. (수집한 것/수집하지 않은 것 포함)")
  ResponseEntity<ApiResponse<UserSealResponse.UserSealListDTO>> getMySeals(
      @Parameter(hidden = true) Authentication authentication,
      @RequestParam(required = false, defaultValue = "NUMBER")
          @Parameter(
              description = "정렬 옵션 (NUMBER: 번호순, RARITY: 희귀도순, LOCATION: 지역순, COLLECTED: 수집순)")
          SortBy sortBy);

  @GetMapping("/products")
  @Operation(summary = "띠부씰 상품 조회", description = "구매 가능한 띠부씰 상품 목록을 조회합니다.")
  ResponseEntity<ApiResponse<SealProductResponse.SealProductListDTO>> getSealProducts();

  @GetMapping("/nearby")
  @Operation(summary = "주변 띠부씰 조회", description = "현재 위치에서 가장 가까운 띠부씰 4개를 조회합니다.")
  ResponseEntity<ApiResponse<SealResponse.NearbySealListDTO>> getNearbySeals(
      @RequestParam @Parameter(description = "현재 위치 위도", required = true, example = "35.8714")
          Double latitude,
      @RequestParam @Parameter(description = "현재 위치 경도", required = true, example = "128.6014")
          Double longitude);

  @PostMapping("/collect")
  @Operation(summary = "띠부씰 획득", description = "위치 인증을 통해 띠부씰을 획득합니다. 200m 이내에 있어야 획득 가능합니다.")
  ResponseEntity<ApiResponse<SealResponse.CollectSealResultDTO>> collectSeal(
      @Parameter(hidden = true) Authentication authentication,
      @RequestParam @Parameter(description = "띠부씰 ID", required = true, example = "1") Long sealId,
      @RequestParam @Parameter(description = "현재 위치 위도", required = true, example = "35.8714")
          Double latitude,
      @RequestParam @Parameter(description = "현재 위치 경도", required = true, example = "128.6014")
          Double longitude);
}
