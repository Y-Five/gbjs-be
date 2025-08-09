/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.weather.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfive.gbjs.domain.weather.dto.response.WeatherResponse;
import com.yfive.gbjs.domain.weather.exception.WeatherErrorStatus;
import com.yfive.gbjs.global.error.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherServiceImpl implements WeatherService {

  /** 서울 지역 기준의 표준 시간대를 나타냅니다. */
  private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

  @Value("${openapi.secret.key}")
  private String serviceKey;

  @Value("${weather.api.url}")
  private String weatherApiUrl;

  private final ObjectMapper objectMapper;
  private final RestClient restClient;

  /**
   * 위도와 경도를 기반으로 기상청 API를 호출하여 날씨 정보를 조회합니다.
   *
   * @param longitude 경도
   * @param latitude 위도
   * @return 조회된 날씨 정보를 담은 WeatherResponse 객체
   */
  @Override
  public WeatherResponse getWeather(Double longitude, Double latitude) {
    // 경도/위도를 기상청 격자 좌표로 변환
    GridCoord gridCoord = convertToGrid(longitude, latitude);

    // 기준 날짜 및 시간 설정
    String baseTime = getBaseTime();
    LocalDate baseDate = LocalDate.now(ZONE_ID);

    if (baseTime.equals("2300")) {
      LocalTime now = LocalTime.now(ZONE_ID);
      if (now.isBefore(LocalTime.of(2, 0))) {
        baseDate = baseDate.minusDays(1);
      }
    }

    // 요청 URL 조합
    UriComponentsBuilder uriBuilder =
        UriComponentsBuilder.fromUriString(weatherApiUrl)
            .queryParam("serviceKey", serviceKey)
            .queryParam("pageNo", 1)
            .queryParam("numOfRows", 1000)
            .queryParam("dataType", "JSON")
            .queryParam("base_date", baseDate.format(DateTimeFormatter.BASIC_ISO_DATE))
            .queryParam("base_time", baseTime)
            .queryParam("nx", gridCoord.getNx())
            .queryParam("ny", gridCoord.getNy());

    try {
      String response =
          restClient.get().uri(uriBuilder.build(true).toUri()).retrieve().body(String.class);

      log.info("response={}", uriBuilder.toUriString());
      log.info("response={}, {}", baseTime, baseDate.format(DateTimeFormatter.BASIC_ISO_DATE));
      if (response == null || response.isBlank()) {
        log.warn("날씨 API 응답이 비어있습니다.");
        throw new CustomException(WeatherErrorStatus.EMPTY_RESPONSE);
      }

      JsonNode root = objectMapper.readTree(response);
      JsonNode items = root.path("response").path("body").path("items").path("item");

      if (items.isMissingNode() || !items.isArray() || items.isEmpty()) {
        log.warn("날씨 정보가 존재하지 않습니다.");
        throw new CustomException(WeatherErrorStatus.ITEM_NOT_FOUND);
      }

      log.info(
          "날씨 정보 조회 성공: baseDate={}, baseTime={}, nx={}, ny={}",
          baseDate,
          baseTime,
          gridCoord.getNx(),
          gridCoord.getNy());

      return parseWeather(items);

    } catch (CustomException e) {
      throw e;
    } catch (JsonProcessingException e) {
      log.error("날씨 JSON 파싱 오류: {}", e.getMessage(), e);
      throw new CustomException(WeatherErrorStatus.PARSING_ERROR);
    } catch (Exception e) {
      log.error(
          "날씨 정보 조회 실패: baseDate={}, baseTime={}, nx={}, ny={}, error={}",
          baseDate,
          baseTime,
          gridCoord.getNx(),
          gridCoord.getNy(),
          e.getMessage(),
          e);
      throw new CustomException(WeatherErrorStatus.API_REQUEST_FAILED);
    }
  }

  /**
   * 위도와 경도를 기상청 격자 좌표로 변환합니다.
   *
   * @param longitude 경도
   * @param latitude 위도
   * @return 변환된 격자 좌표 (GridCoord)
   */
  @Override
  public GridCoord convertToGrid(double longitude, double latitude) {
    double RE = 6371.00877; // Earth radius (km)
    double GRID = 5.0; // Grid spacing (km)
    double SLAT1 = 30.0; // Projection latitude 1 (degree)
    double SLAT2 = 60.0; // Projection latitude 2 (degree)
    double OLON = 126.0; // Reference longitude (degree)
    double OLAT = 38.0; // Reference latitude (degree)
    double XO = 43; // Reference point X coordinate
    double YO = 136; // Reference point Y coordinate

    double DEGRAD = Math.PI / 180.0;

    double re = RE / GRID;
    double slat1 = SLAT1 * DEGRAD;
    double slat2 = SLAT2 * DEGRAD;
    double olon = OLON * DEGRAD;
    double olat = OLAT * DEGRAD;

    double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
    sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);

    double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
    sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;

    double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
    ro = re * sf / Math.pow(ro, sn);

    double ra = Math.tan(Math.PI * 0.25 + latitude * DEGRAD * 0.5);
    ra = re * sf / Math.pow(ra, sn);

    double theta = longitude * DEGRAD - olon;
    if (theta > Math.PI) {
      theta -= 2.0 * Math.PI;
    }
    if (theta < -Math.PI) {
      theta += 2.0 * Math.PI;
    }
    theta *= sn;

    int x = (int) Math.floor(ra * Math.sin(theta) + XO + 0.5);
    int y = (int) Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);

    return new GridCoord(x, y);
  }

  /**
   * 현재 시각 기준으로 가장 최근 발표 기준 시각을 반환합니다.
   *
   * @return 발표 기준 시각 (HHmm 형식)
   */
  @Override
  public String getBaseTime() {
    LocalTime now = LocalTime.now(ZONE_ID);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmm");

    List<String> baseTimes =
        List.of("2300", "2000", "1700", "1400", "1100", "0800", "0500", "0200");

    for (String base : baseTimes) {
      LocalTime baseTime = LocalTime.parse(base, formatter).plusMinutes(10);
      if (now.isAfter(baseTime)) {
        return base;
      }
    }

    return "2300";
  }

  /**
   * 기상청 API에서 받은 JSON 데이터를 파싱하여 WeatherResponse 객체로 변환합니다.
   *
   * @param items 기상청 응답의 item 배열 노드
   * @return 파싱된 날씨 정보 객체
   */
  @Override
  public WeatherResponse parseWeather(JsonNode items) {
    String temperature = null;
    String skyStatus = null;
    String lowestTemperature = null;
    String highestTemperature = null;
    String precipitation = null;
    String precipitationType = null;
    String weather;

    for (JsonNode item : items) {
      String category = item.get("category").asText();
      String value = item.get("fcstValue").asText();

      switch (category) {
        case "TMP" -> temperature = value;
        case "SKY" -> skyStatus = mapSkyStatus(value);
        case "TMN" -> lowestTemperature = value;
        case "TMX" -> highestTemperature = value;
        case "POP" -> precipitation = value;
        case "PTY" -> precipitationType = mapPrecipitationType(value);
      }
    }

    if (Objects.equals(precipitationType, "없음")) {
      weather = skyStatus;
    } else {
      weather = precipitationType;
    }

    return WeatherResponse.builder()
        .lowestTemperature(lowestTemperature)
        .highestTemperature(highestTemperature)
        .temperature(temperature)
        .weather(weather)
        .precipitation(precipitation)
        .build();
  }

  /**
   * 하늘 상태 코드를 사람이 이해할 수 있는 문자열로 변환합니다.
   *
   * @param code SKY 코드 값
   * @return 하늘 상태 (예: 맑음, 흐림 등)
   */
  @Override
  public String mapSkyStatus(String code) {
    return switch (code) {
      case "1" -> "맑음";
      case "3" -> "구름많음";
      case "4" -> "흐림";
      default -> "알 수 없음";
    };
  }

  /**
   * 강수 형태 코드를 사람이 이해할 수 있는 문자열로 변환합니다.
   *
   * @param code PTY 코드 값
   * @return 강수 형태 (예: 비, 눈, 없음 등)
   */
  @Override
  public String mapPrecipitationType(String code) {
    return switch (code) {
      case "0" -> "없음";
      case "1" -> "비";
      case "2" -> "비/눈";
      case "3" -> "눈";
      case "4" -> "소나기";
      default -> "알 수 없음";
    };
  }
}
