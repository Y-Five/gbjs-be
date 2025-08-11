/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.festival.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfive.gbjs.domain.festival.dto.response.FestivalDetailResponse;
import com.yfive.gbjs.domain.festival.dto.response.FestivalResponse;
import com.yfive.gbjs.domain.festival.exception.FestivalErrorStatus;
import com.yfive.gbjs.global.error.exception.CustomException;
import com.yfive.gbjs.global.page.dto.response.PageResponse;
import com.yfive.gbjs.global.page.exception.PageErrorStatus;
import com.yfive.gbjs.global.page.mapper.PageMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
  private final PageMapper pageMapper;

  @Override
  public PageResponse<FestivalResponse> getFestivalsByRegion(String region, Pageable pageable) {

    List<FestivalResponse> festivalResponses = fetchFestivalListByRegion(region);

    long offset = pageable.getOffset();
    long totalElements = festivalResponses.size();
    int pageSize = pageable.getPageSize();

    if (totalElements == 0) {
      Page<FestivalResponse> page = new PageImpl<>(java.util.Collections.emptyList(), pageable, 0);

      return pageMapper.toFestivalPageResponse(page);
    }

    if (offset > Integer.MAX_VALUE) {
      throw new CustomException(PageErrorStatus.PAGE_NOT_FOUND);
    }

    int start = (int) offset;
    if (start >= totalElements) {
      throw new CustomException(PageErrorStatus.PAGE_NOT_FOUND);
    }

    int end = (int) Math.min(offset + pageSize, totalElements);
    List<FestivalResponse> pagedList = festivalResponses.subList(start, end);
    Page<FestivalResponse> page = new PageImpl<>(pagedList, pageable, totalElements);

    return pageMapper.toFestivalPageResponse(page);
  }

  @Override
  public FestivalDetailResponse getFestivalById(String id) {
    try {
      JsonNode itemNode1 = fetchFestivalDetail("detailCommon2", id, null);
      JsonNode itemNode2 = fetchFestivalDetail("detailIntro2", id, 15);

      FestivalDetailResponse detailResponse1 =
          objectMapper.treeToValue(itemNode1, FestivalDetailResponse.class);
      FestivalDetailResponse detailResponse2 =
          objectMapper.treeToValue(itemNode2, FestivalDetailResponse.class);

      String url = extractUrlFromHtml(detailResponse1.getHomepageUrl());

      detailResponse1.setStartDate(detailResponse2.getStartDate());
      detailResponse1.setEndDate(detailResponse2.getEndDate());
      detailResponse1.setHomepageUrl(url);

      return detailResponse1;
    } catch (Exception e) {
      log.error("축제 목록 파싱 실패", e);
      throw new CustomException(FestivalErrorStatus.FESTIVAL_API_ERROR);
    }
  }

  private JsonNode fetchFestivalDetail(String path, String id, Integer contentTypeId) {
    UriComponentsBuilder uriBuilder =
        UriComponentsBuilder.fromUriString(festivalApiUrl + "/" + path)
            .queryParam("serviceKey", serviceKey)
            .queryParam("MobileOS", "WEB")
            .queryParam("MobileApp", "gbjs")
            .queryParam("_type", "JSON")
            .queryParam("contentId", id);

    if (contentTypeId != null) {
      uriBuilder.queryParam("contentTypeId", contentTypeId);
    }

    String response =
        restClient.get().uri(uriBuilder.build(true).toUri()).retrieve().body(String.class);

    if (response == null || response.isBlank()) {
      log.error("빈 응답 수신");
      throw new CustomException(FestivalErrorStatus.FESTIVAL_API_ERROR);
    }

    if (isXmlOrHtml(response)) {
      throw new CustomException(FestivalErrorStatus.FESTIVAL_API_ERROR);
    }

    try {
      JsonNode root = objectMapper.readTree(response);
      JsonNode itemNode = root.path("response").path("body").path("items").path("item");

      if (itemNode.isEmpty()) {
        throw new CustomException(FestivalErrorStatus.FESTIVAL_API_ERROR);
      }
      return itemNode.get(0);
    } catch (Exception e) {
      log.error("축제 상세 파싱 실패", e);
      throw new CustomException(FestivalErrorStatus.FESTIVAL_API_ERROR);
    }
  }

  private String extractUrlFromHtml(String html) {
    if (html == null) {
      return null;
    }
    Pattern pattern = Pattern.compile("<a\\s+href\\s*=\\s*\"([^\"]+)\"");
    Matcher matcher = pattern.matcher(html);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return html;
  }

  private List<FestivalResponse> fetchFestivalListByRegion(String region) {
    LocalDate baseDate = LocalDate.now();

    UriComponentsBuilder uriBuilder =
        UriComponentsBuilder.fromUriString(festivalApiUrl + "/searchFestival2")
            .queryParam("serviceKey", serviceKey)
            .queryParam("numOfRows", 1000)
            .queryParam("MobileOS", "WEB")
            .queryParam("MobileApp", "gbjs")
            .queryParam("_type", "JSON")
            .queryParam("eventStartDate", baseDate.format(DateTimeFormatter.BASIC_ISO_DATE))
            .queryParam(
                "eventEndDate", baseDate.plusMonths(6).format(DateTimeFormatter.BASIC_ISO_DATE))
            .queryParam("areaCode", 35)
            .queryParam("sigunguCode", getSiGunGuCode(region));

    String response =
        restClient.get().uri(uriBuilder.build(true).toUri()).retrieve().body(String.class);

    if (response == null || response.isBlank()) {
      log.error("빈 응답 수신");
      throw new CustomException(FestivalErrorStatus.FESTIVAL_API_ERROR);
    }

    if (isXmlOrHtml(response)) {
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
  private Integer getSiGunGuCode(String region) {
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

  private boolean isXmlOrHtml(String response) {
    return response != null && response.trim().startsWith("<");
  }
}
