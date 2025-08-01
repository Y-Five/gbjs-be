package com.yfive.gbjs.domain.spot.controller;

import com.yfive.gbjs.domain.spot.dto.response.SpotPageResponse;
import com.yfive.gbjs.domain.spot.dto.response.SpotResponse;
import com.yfive.gbjs.domain.spot.service.SpotService;
import com.yfive.gbjs.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SpotControllerImpl implements SpotController {

  private final SpotService spotService;

  @Override
  public ResponseEntity<ApiResponse<SpotPageResponse>> getSpotsByKeyword(
      @RequestParam String keyword,
      @RequestParam Integer pageNum,
      @RequestParam Integer pageSize,
      @RequestParam String sortBy,
      @RequestParam Double longitude,
      @RequestParam Double latitude) {

    Pageable pageable = PageRequest.of(pageNum, pageSize);
    SpotPageResponse spotListResponse = spotService.getSpotsByKeyword(keyword, pageable,
        sortBy, longitude, latitude);

    return ResponseEntity.ok(ApiResponse.success(spotListResponse));
  }

  @Override
  public ResponseEntity<ApiResponse<SpotResponse>> getSpotByContentId(
      @RequestParam Long contentId,
      @RequestParam Double longitude,
      @RequestParam Double latitude) {
    SpotResponse spotResponse = spotService.getSpotByContentId(contentId, longitude, latitude);

    return ResponseEntity.ok(ApiResponse.success(spotResponse));
  }
}
