/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

import com.yfive.gbjs.domain.seal.dto.response.SealProductResponse;
import com.yfive.gbjs.domain.seal.dto.response.SealResponse;
import com.yfive.gbjs.domain.seal.dto.response.UserSealResponse;
import com.yfive.gbjs.domain.seal.service.SealService;
import com.yfive.gbjs.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 띠부씰 컨트롤러 구현체 띠부씰 관련 HTTP 요청을 처리하고 적절한 서비스로 위임 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SealControllerImpl implements SealController {

  private final SealService sealService;

  @Override
  public ResponseEntity<ApiResponse<SealResponse.SealListDTO>> getAllSeals() {
    SealResponse.SealListDTO response = sealService.getAllSeals();
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Override
  public ResponseEntity<ApiResponse<UserSealResponse.UserSealListDTO>> getMySeals(
      Authentication authentication) {
    Long userId = Long.valueOf(authentication.getName());
    UserSealResponse.UserSealListDTO response = sealService.getUserSeals(userId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Override
  public ResponseEntity<ApiResponse<SealProductResponse.SealProductListDTO>> getSealProducts() {
    SealProductResponse.SealProductListDTO response = sealService.getSealProducts();
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
