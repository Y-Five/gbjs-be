/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.festival.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yfive.gbjs.domain.festival.dto.response.FestivalDetailResponse;
import com.yfive.gbjs.domain.festival.dto.response.FestivalResponse;
import com.yfive.gbjs.domain.festival.service.FestivalService;
import com.yfive.gbjs.global.common.response.ApiResponse;
import com.yfive.gbjs.global.error.exception.CustomException;
import com.yfive.gbjs.global.page.dto.response.PageResponse;
import com.yfive.gbjs.global.page.exception.PageErrorStatus;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class FestivalControllerImpl implements FestivalController {

  private final FestivalService festivalService;

  @Override
  public ResponseEntity<ApiResponse<PageResponse<FestivalResponse>>> getFestivalsByRegion(
      @RequestParam String region, @RequestParam Integer pageNum, @RequestParam Integer pageSize) {

    if (pageNum < 1) {
      throw new CustomException(PageErrorStatus.PAGE_NOT_FOUND);
    }
    if (pageSize < 1) {
      throw new CustomException(PageErrorStatus.PAGE_SIZE_ERROR);
    }

    Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
    PageResponse<FestivalResponse> festivalListResponse =
        festivalService.getFestivalsByRegion(region, pageable);

    return ResponseEntity.ok(ApiResponse.success(festivalListResponse));
  }

  @Override
  public ResponseEntity<ApiResponse<FestivalDetailResponse>> getFestivalById(
      @PathVariable String id) {

    FestivalDetailResponse response = festivalService.getFestivalById(id);

    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
