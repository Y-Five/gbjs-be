/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.controller;

import com.yfive.gbjs.domain.seal.dto.response.SealListResponse;
import com.yfive.gbjs.domain.seal.dto.response.SealProductListResponse;
import com.yfive.gbjs.domain.seal.dto.response.UserSealListResponse;
import com.yfive.gbjs.domain.seal.service.SealService;
import com.yfive.gbjs.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * 띠부씰 컨트롤러 구현체
 * 띠부씰 관련 HTTP 요청을 처리하고 적절한 서비스로 위임
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SealControllerImpl implements SealController {

  private final SealService sealService;

  @Override
  public ResponseEntity<ApiResponse<SealListResponse>> getAllSeals() {
    log.info("Get all seals request");
    SealListResponse response = sealService.getAllSeals();
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Override
  public ResponseEntity<ApiResponse<UserSealListResponse>> getUserSeals(Long userId) {
    log.info("Get user seals request for userId: {}", userId);
    UserSealListResponse response = sealService.getUserSeals(userId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Override
  public ResponseEntity<ApiResponse<SealProductListResponse>> getSealProducts() {
    log.info("Get seal products request");
    SealProductListResponse response = sealService.getSealProducts();
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
