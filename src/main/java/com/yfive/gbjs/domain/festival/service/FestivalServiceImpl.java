/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.festival.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfive.gbjs.domain.festival.dto.response.FestivalListResponse;
import com.yfive.gbjs.domain.festival.dto.response.FestivalResponse;
import com.yfive.gbjs.domain.festival.exception.FestivalErrorStatus;
import com.yfive.gbjs.global.error.exception.CustomException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class FestivalServiceImpl implements FestivalService {

  @Value("${openapi.secret.key}")
  private String serviceKey;

  @Value("${tourist.api.url}")
  private String festivalApiUrl;

  private final ObjectMapper objectMapper;
  private final RestClient restClient;

  @Override
  public FestivalListResponse getFestivalsByRegion(
      String region, Integer startIndex, Integer pageSize) {
    List<FestivalResponse> festivalResponses = fetchFestivalListByRegion(region);

    if (startIndex >= festivalResponses.size()) {
      return FestivalListResponse.builder()
          .totalCount(festivalResponses.size())
          .festivalList(List.of())
          .nextIndex(startIndex)
          .build();
    }

    int toIndex = Math.min(startIndex + pageSize, festivalResponses.size());
    List<FestivalResponse> pagedList = festivalResponses.subList(startIndex, toIndex);

    return FestivalListResponse.builder()
        .totalCount(festivalResponses.size())
        .festivalList(pagedList)
        .nextIndex(toIndex)
        .build();
  }

  @Cacheable(value = "festivals", key = "#region")
  public List<FestivalResponse> fetchFestivalListByRegion(String region) {
    LocalDate baseDate = LocalDate.now();

    UriComponentsBuilder uriBuilder =
        UriComponentsBuilder.fromHttpUrl(festivalApiUrl + "/searchFestival2")
            .queryParam("serviceKey", serviceKey)
            .queryParam("numOfRows", 1000)
            .queryParam("MobileOS", "WEB")
            .queryParam("MobileApp", "gbjs")
            .queryParam("_type", "JSON")
            .queryParam("eventStartDate", baseDate.format(DateTimeFormatter.BASIC_ISO_DATE))
            .queryParam(
                "eventEndDate", baseDate.plusMonths(6).format(DateTimeFormatter.BASIC_ISO_DATE))
            .queryParam("sigunguCode", getSiGunGuCode(region));

    String response =
        restClient.get().uri(uriBuilder.build(true).toUri()).retrieve().body(String.class);

    if (response == null || response.isBlank()) {
      log.error("빈 응답 수신");
      throw new CustomException(FestivalErrorStatus.FESTIVAL_API_ERROR);
    }

    if (response.trim().startsWith("<?xml") || response.trim().startsWith("<")) {
      throw new CustomException(FestivalErrorStatus.FESTIVAL_API_ERROR);
    }

    try {
      JsonNode root = objectMapper.readTree(response);
      JsonNode items = root.path("response").path("body").path("items").path("item");

      List<FestivalResponse> festivals = new ArrayList<>();
      for (JsonNode item : items) {
        FestivalResponse festival = objectMapper.treeToValue(item, FestivalResponse.class);
        festivals.add(festival);
      }

      festivals.sort(Comparator.comparing(FestivalResponse::getEndDate));
      return festivals;

    } catch (Exception e) {
      log.error("축제 목록 파싱 실패", e);
      throw new CustomException(FestivalErrorStatus.FESTIVAL_API_ERROR);
    }
  }

  /**
   * 지역명에 해당하는 시군구 코드를 반환한다.
   *
   * @param region 지역 이름
   * @return 시군구 코드
   * @throws CustomException 잘못된 지역명일 경우
   */
  @Override
  public Integer getSiGunGuCode(String region) {
    return switch (region) {
      case "경산시" -> 1;
      case "경주시" -> 2;
      case "고령군" -> 3;
      case "구미시" -> 4;
      case "김천시" -> 6;
      case "문경시" -> 7;
      case "봉화군" -> 8;
      case "상주시" -> 9;
      case "성주군" -> 10;
      case "안동시" -> 11;
      case "영덕군" -> 12;
      case "영양군" -> 13;
      case "영주시" -> 14;
      case "영천시" -> 15;
      case "예천군" -> 16;
      case "울릉군" -> 17;
      case "울진군" -> 18;
      case "의성군" -> 19;
      case "청도군" -> 20;
      case "청송군" -> 21;
      case "칠곡군" -> 22;
      case "포항시" -> 23;
      default -> throw new CustomException(FestivalErrorStatus.INVALID_REGION);
    };
  }
}
