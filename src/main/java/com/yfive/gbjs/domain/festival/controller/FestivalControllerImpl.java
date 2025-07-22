package com.yfive.gbjs.domain.festival.controller;

import com.yfive.gbjs.domain.festival.dto.response.FestivalListResponse;
import com.yfive.gbjs.domain.festival.service.FestivalService;
import com.yfive.gbjs.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FestivalControllerImpl implements FestivalController {

  private final FestivalService festivalService;

  @Override
  public ResponseEntity<ApiResponse<FestivalListResponse>> getFestivalsByRegion(
      @Parameter(description = "지역", example = "안동시") @RequestParam String region,
      @Parameter(description = "첫 번째 인덱스 값", example = "4") @RequestParam Integer startIndex,
      @Parameter(description = "페이지 크기", example = "3") @RequestParam Integer pageSize) {

    FestivalListResponse response = festivalService.getFestivalsByRegion(region, startIndex,
        pageSize);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
