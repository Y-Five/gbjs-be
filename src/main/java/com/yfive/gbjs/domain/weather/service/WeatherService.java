/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.weather.service;

import com.yfive.gbjs.domain.weather.dto.response.WeatherResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface WeatherService {

  /**
   * 현재 위치(위도, 경도)를 기반으로 날씨 정보를 조회합니다.
   *
   * @param longitude 경도 값 (예: 127.12345)
   * @param latitude 위도 값 (예: 37.12345)
   * @return 날씨 정보를 담은 WeatherResponse 객체
   */
  WeatherResponse getWeather(Double longitude, Double latitude);

  /** 기상청 격자 좌표를 표현하는 클래스입니다. */
  @Getter
  @AllArgsConstructor
  class GridCoord {

    private final int nx;
    private final int ny;
  }
}
