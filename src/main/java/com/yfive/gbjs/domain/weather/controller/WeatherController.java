/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.weather.controller;

import com.yfive.gbjs.domain.weather.dto.response.WeatherResponse;
import com.yfive.gbjs.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "날씨", description = "날씨 관련 API (공공데이터 기상청 OpenAPI)")
@RequestMapping("/api/weathers")
public interface WeatherController {

  @GetMapping
  @Operation(summary = "좌표 기반 날씨 조회", description = "위도/경도를 기반으로 기상청 단기 예보 응답 반환")
  public ResponseEntity<ApiResponse<WeatherResponse>> getWeather(
      @Parameter(description = "경도", example = "128.516667") @RequestParam(defaultValue = "128.516667") Double longitude,
      @Parameter(description = "위도", example = "36.583333") @RequestParam(defaultValue = "36.583333") Double latitude);
}
