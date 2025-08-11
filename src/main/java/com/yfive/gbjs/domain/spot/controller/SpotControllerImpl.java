/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.spot.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yfive.gbjs.domain.spot.dto.response.SpotDetailResponse;
import com.yfive.gbjs.domain.spot.dto.response.SpotResponse;
import com.yfive.gbjs.domain.spot.entity.SortBy;
import com.yfive.gbjs.domain.spot.service.SpotService;
import com.yfive.gbjs.global.common.response.ApiResponse;
import com.yfive.gbjs.global.common.response.PageResponse;
import com.yfive.gbjs.global.error.exception.CustomException;
import com.yfive.gbjs.global.page.exception.PageErrorStatus;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SpotControllerImpl implements SpotController {

  private final SpotService spotService;

  @Override
  public ResponseEntity<ApiResponse<PageResponse<SpotResponse>>> getSpotsByKeyword(
      @RequestParam Integer pageNum,
      @RequestParam Integer pageSize,
      @RequestParam String keyword,
      @RequestParam SortBy sortBy,
      @RequestParam Double latitude,
      @RequestParam Double longitude) {

    if (pageNum < 1) {
      throw new CustomException(PageErrorStatus.PAGE_NOT_FOUND);
    }
    if (pageSize < 1) {
      throw new CustomException(PageErrorStatus.PAGE_SIZE_ERROR);
    }

    Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
    PageResponse<SpotResponse> spotListResponse;

    if (sortBy == SortBy.DISTANCE) {
      spotListResponse =
          spotService.getSpotsByKeywordSortedByDistance(pageable, keyword, latitude, longitude);
    } else {
      spotListResponse = spotService.getSpotsByKeyword(pageable, keyword, latitude, longitude);
    }

    return ResponseEntity.ok(ApiResponse.success(spotListResponse));
  }

  @Override
  public ResponseEntity<ApiResponse<SpotDetailResponse>> getSpotByContentId(
      String contentId, @RequestParam Double latitude, @RequestParam Double longitude) {
    SpotDetailResponse spotDetailResponse =
        spotService.getSpotByContentId(contentId, latitude, longitude);

    return ResponseEntity.ok(ApiResponse.success(spotDetailResponse));
  }
}
