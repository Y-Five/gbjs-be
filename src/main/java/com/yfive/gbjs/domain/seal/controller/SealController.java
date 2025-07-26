/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yfive.gbjs.domain.seal.dto.response.SealProductResponse;
import com.yfive.gbjs.domain.seal.dto.response.SealResponse;
import com.yfive.gbjs.domain.seal.dto.response.UserSealResponse;
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
  ResponseEntity<ApiResponse<SealResponse.SealListDTO>> getAllSeals();

  @GetMapping("/users/{userId}")
  @Operation(summary = "회원 띠부씰 조회", description = "특정 회원의 띠부씰 수집 현황을 조회합니다. (수집한 것/수집하지 않은 것 포함)")
  ResponseEntity<ApiResponse<UserSealResponse.UserSealListDTO>> getUserSeals(
      @Parameter(description = "회원 ID", example = "1", required = true) @PathVariable Long userId);

  @GetMapping("/products")
  @Operation(summary = "띠부씰 상품 조회", description = "구매 가능한 띠부씰 상품 목록을 조회합니다.")
  ResponseEntity<ApiResponse<SealProductResponse.SealProductListDTO>> getSealProducts();
}
