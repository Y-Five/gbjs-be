/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.spot.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yfive.gbjs.domain.spot.dto.response.SpotResponse;
import com.yfive.gbjs.domain.spot.service.SpotService;
import com.yfive.gbjs.global.common.response.ApiResponse;
import com.yfive.gbjs.global.common.response.PageResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SpotControllerImpl implements SpotController {

  private final SpotService spotService;

  @Override
  public ResponseEntity<ApiResponse<PageResponse<SpotResponse>>> getSpotsByKeyword(
      @RequestParam String keyword,
      @RequestParam Integer pageNum,
      @RequestParam Integer pageSize,
      @RequestParam String sortBy,
      @RequestParam Double latitude,
      @RequestParam Double longitude) {

    Pageable pageable = PageRequest.of(pageNum, pageSize);
    PageResponse<SpotResponse> spotListResponse =
        spotService.getSpotsByKeyword(keyword, pageable, sortBy, latitude, longitude);

    return ResponseEntity.ok(ApiResponse.success(spotListResponse));
  }

  @Override
  public ResponseEntity<ApiResponse<SpotResponse>> getSpotByContentId(
      String contentId, @RequestParam Double latitude, @RequestParam Double longitude) {
    SpotResponse spotResponse = spotService.getSpotByContentId(contentId, latitude, longitude);

    return ResponseEntity.ok(ApiResponse.success(spotResponse));
  }
}
