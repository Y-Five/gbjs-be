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
import com.yfive.gbjs.domain.seal.entity.SortBy;
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
  public ResponseEntity<ApiResponse<SealResponse.SealListDTO>> getAllSeals(SortBy sortBy) {
    SealResponse.SealListDTO response = sealService.getAllSeals(sortBy);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Override
  public ResponseEntity<ApiResponse<UserSealResponse.UserSealListDTO>> getMySeals(
      Authentication authentication, SortBy sortBy) {
    UserSealResponse.UserSealListDTO response = sealService.getUserSeals(sortBy);

    // 획득한 띠부씰이 있는지 확인하여 적절한 메시지 설정
    boolean hasCollected = response.getSeals().stream().anyMatch(seal -> seal.isCollected());
    String message = hasCollected ? null : "아직 획득한 띠부씰이 없습니다.";

    return ResponseEntity.ok(ApiResponse.success(response, message));
  }

  @Override
  public ResponseEntity<ApiResponse<SealProductResponse.SealProductListDTO>> getSealProducts() {
    SealProductResponse.SealProductListDTO response = sealService.getSealProducts();
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Override
  public ResponseEntity<ApiResponse<SealResponse.NearbySealListDTO>> getNearbySeals(
      Double latitude, Double longitude) {
    SealResponse.NearbySealListDTO response = sealService.getNearbySeals(latitude, longitude);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Override
  public ResponseEntity<ApiResponse<SealResponse.CollectSealResultDTO>> collectSeal(
      Authentication authentication, Long sealId, Double latitude, Double longitude) {
    SealResponse.CollectSealResultDTO response =
        sealService.collectSeal(sealId, latitude, longitude);

    if (response.isSuccess()) {
      return ResponseEntity.ok(ApiResponse.success(response, "띠부씰을 성공적으로 획득했습니다!"));
    } else {
      // 지역에 따른 실패 메시지 (울릉도는 2km, 나머지는 500m)
      String failMessage = sealService.getFailureMessage(sealId);
      return ResponseEntity.ok(ApiResponse.success(response, failMessage));
    }
  }

  @Override
  public ResponseEntity<ApiResponse<Void>> deleteCollectedSeal(
      Authentication authentication, Long sealId) {
    sealService.deleteCollectedSeal(sealId);
    return ResponseEntity.ok(ApiResponse.success(null, "띠부씰이 성공적으로 삭제되었습니다."));
  }
}
