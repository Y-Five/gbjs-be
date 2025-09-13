/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tradition.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.yfive.gbjs.domain.tradition.dto.request.TraditionRequest;
import com.yfive.gbjs.domain.tradition.dto.response.TraditionResponse;
import com.yfive.gbjs.domain.tradition.entity.TraditionType;
import com.yfive.gbjs.domain.tradition.service.TraditionService;
import com.yfive.gbjs.global.common.response.ApiResponse;
import com.yfive.gbjs.global.error.exception.CustomException;
import com.yfive.gbjs.global.page.dto.response.PageResponse;
import com.yfive.gbjs.global.page.exception.PageErrorStatus;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TraditionControllerImpl implements TraditionController {

  private final TraditionService traditionService;

  @Override
  public ResponseEntity<ApiResponse<TraditionResponse>> createTradition(
      @RequestParam TraditionType type, @RequestPart("tradition") @Valid TraditionRequest request) {

    TraditionResponse response = traditionService.createTradition(type, request);

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Override
  public ResponseEntity<ApiResponse<PageResponse<TraditionResponse>>> getTraditions(
      @RequestParam TraditionType type,
      @RequestParam Integer pageNum,
      @RequestParam Integer pageSize) {

    if (pageNum < 1) {
      throw new CustomException(PageErrorStatus.PAGE_NOT_FOUND);
    }
    if (pageSize < 1) {
      throw new CustomException(PageErrorStatus.PAGE_SIZE_ERROR);
    }

    Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
    PageResponse<TraditionResponse> traditionListResponse =
        traditionService.getTraditionsByType(type, pageable);

    return ResponseEntity.ok(ApiResponse.success(traditionListResponse));
  }

  @Override
  public ResponseEntity<ApiResponse<TraditionResponse>> updateTradition(
      @PathVariable Long id, @RequestPart("tradition") @Valid TraditionRequest request) {

    TraditionResponse traditionResponse = traditionService.updateTradition(id, request);

    return ResponseEntity.ok(ApiResponse.success(traditionResponse));
  }

  @Override
  public ResponseEntity<ApiResponse<String>> deleteTradition(@PathVariable Long id) {

    traditionService.deleteTradition(id);

    return ResponseEntity.ok(ApiResponse.success(id + "번 식별자 전통문화가 정상적으로 삭제되었습니다."));
  }
}
