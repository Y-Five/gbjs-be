/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
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

  /**
   * 위도, 경도를 기상청 격자 좌표로 변환합니다.
   *
   * @param longitude 경도 값
   * @param latitude 위도 값
   * @return 변환된 격자 좌표를 담은 GridCoord 객체
   */
  GridCoord convertToGrid(double longitude, double latitude);

  /**
   * 기상청 API 조회를 위한 기준 시각을 반환합니다.
   *
   * @return 기준 시각 (예: "0200", "0500" 등)
   */
  String getBaseTime();

  /**
   * 기상청에서 받아온 JSON 응답 데이터를 WeatherResponse 객체로 파싱합니다.
   *
   * @param items 기상청 JSON 응답의 items 노드
   * @return 파싱된 WeatherResponse 객체
   */
  WeatherResponse parseWeather(JsonNode items);

  /**
   * 기상청 코드값에 해당하는 하늘 상태를 반환합니다.
   *
   * @param code 하늘 상태 코드
   * @return 사람이 읽을 수 있는 하늘 상태 문자열
   */
  String mapSkyStatus(String code);

  /**
   * 기상청 코드값에 해당하는 강수 형태를 반환합니다.
   *
   * @param code 강수 형태 코드
   * @return 사람이 읽을 수 있는 강수 형태 문자열
   */
  String mapPrecipitationType(String code);

  /** 기상청 격자 좌표를 표현하는 클래스입니다. */
  @Getter
  @AllArgsConstructor
  class GridCoord {

    private final int nx;
    private final int ny;
  }
}
