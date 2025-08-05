/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tradition.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yfive.gbjs.domain.tradition.dto.request.TraditionRequest;
import com.yfive.gbjs.domain.tradition.dto.response.TraditionResponse;
import com.yfive.gbjs.domain.tradition.entity.TraditionType;
import com.yfive.gbjs.domain.tradition.service.TraditionService;
import com.yfive.gbjs.global.common.response.ApiResponse;
import com.yfive.gbjs.global.common.response.PageResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TraditionControllerImpl implements TraditionController {

  private final TraditionService traditionService;

  @Override
  public ResponseEntity<ApiResponse<TraditionResponse>> createTradition(
      @RequestParam TraditionType type,
      @RequestBody @Valid TraditionRequest request,
      MultipartFile image) {

    TraditionResponse response = traditionService.createTradition(type, request, image);

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Override
  public ResponseEntity<ApiResponse<PageResponse<TraditionResponse>>> getTraditions(
      @RequestParam TraditionType type,
      @RequestParam Integer pageNum,
      @RequestParam Integer pageSize) {

    Pageable pageable = PageRequest.of(pageNum, pageSize);
    PageResponse<TraditionResponse> traditions = traditionService.getTraditions(type, pageable);

    return ResponseEntity.ok(ApiResponse.success(traditions));
  }

  @Override
  public ResponseEntity<ApiResponse<TraditionResponse>> updateTradition(
      @PathVariable Long id, @RequestBody @Valid TraditionRequest request, String imageUrl) {

    TraditionResponse traditionResponse = traditionService.updateTradition(id, request, imageUrl);

    return ResponseEntity.ok(ApiResponse.success(traditionResponse));
  }

  @Override
  public ResponseEntity<ApiResponse<String>> deleteTradition(@PathVariable Long id) {

    traditionService.deleteTradition(id);

    return ResponseEntity.ok(ApiResponse.success(id + "번 식별자 전통문화가 정상적으로 삭제되었습니다."));
  }
}
