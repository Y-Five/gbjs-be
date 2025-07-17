package com.yfive.gbjs.domain.weather.controller;

import com.yfive.gbjs.domain.weather.dto.response.WeatherResponse;
import com.yfive.gbjs.domain.weather.service.WeatherService;
import com.yfive.gbjs.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weathers")
@Tag(name = "날씨", description = "날씨 관리 API (공공데이터 기상청 OpenAPI)")
public class WeatherController {

  private final WeatherService weatherService;

  @Operation(summary = "좌표 기반 날씨 조회", description = "위도/경도를 기반으로 기상청 단기 예보 응답 반환")
  @GetMapping
  public ResponseEntity<ApiResponse<WeatherResponse>> getWeather(
      @Parameter(description = "경도", example = "128.505832") @RequestParam Double longitude,
      @Parameter(description = "위도", example = "36.5759985") @RequestParam Double latitude
  ) {
    WeatherResponse response = weatherService.getWeather(longitude, latitude);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}