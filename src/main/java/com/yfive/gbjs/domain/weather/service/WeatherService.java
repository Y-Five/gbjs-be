/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.yfive.gbjs.domain.weather.dto.response.WeatherResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

public interface WeatherService {

  WeatherResponse getWeather(Double longitude, Double latitude);

  GridCoord convertToGrid(double longitude, double latitude);

  String getBaseTime();

  WeatherResponse parseWeather(JsonNode items);

  String mapSkyStatus(String code);

  String mapPrecipitationType(String code);

  @Getter
  @AllArgsConstructor
  class GridCoord {

    private final int nx;
    private final int ny;
  }
}
