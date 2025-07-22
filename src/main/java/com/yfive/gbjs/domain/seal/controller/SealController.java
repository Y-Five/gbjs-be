package com.yfive.gbjs.domain.seal.controller;

import com.yfive.gbjs.domain.seal.dto.response.SealListResponse;
import com.yfive.gbjs.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "띠부씰", description = "띠부씰 관련 API")
@RequestMapping("/api/seals")
public interface SealController {

  @PostMapping
  @Operation(summary = "선택 날짜, 지역 기반 띠부씰 코스 생성", description = "선택한 지역과 여행 일수를 기반으로 해당 지역에 대한 띠부씰 리스트(여행 코스) 반환")
  ResponseEntity<ApiResponse<SealListResponse>> getSealsByRegion();

}
