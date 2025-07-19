package com.yfive.gbjs.domain.weather.controller;

import com.yfive.gbjs.domain.weather.dto.response.WeatherResponse;
import com.yfive.gbjs.domain.weather.service.WeatherService;
import com.yfive.gbjs.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 날씨 컨트롤러 구현체
 *
 * <p>날씨 관련 API 엔드포인트를 제공합니다.
 *
 * @author YFIVE
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class WeatherControllerImpl implements WeatherController {

  private final WeatherService weatherService;

  /**
   * 현재 위치(위도, 경도)를 기준으로 날씨 정보를 조회합니다.
   *
   * @param longitude 경도 값 (예: 128.505832)
   * @param latitude  위도 값 (예: 36.5759985)
   * @return 해당 위치의 날씨 정보를 포함한 응답 객체
   */
  @Override
  public ResponseEntity<ApiResponse<WeatherResponse>> getWeather(
      @Parameter(description = "경도", example = "128.505832") @RequestParam Double longitude,
      @Parameter(description = "위도", example = "36.5759985") @RequestParam Double latitude) {
    WeatherResponse response = weatherService.getWeather(longitude, latitude);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
