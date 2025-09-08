/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.spot.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfive.gbjs.domain.guide.entity.AudioGuide;
import com.yfive.gbjs.domain.guide.repository.AudioGuideRepository;
import com.yfive.gbjs.domain.spot.dto.response.SpotDetailResponse;
import com.yfive.gbjs.domain.spot.dto.response.SpotResponse;
import com.yfive.gbjs.domain.spot.dto.response.SpotTtsResponse;
import com.yfive.gbjs.domain.spot.entity.SearchBy;
import com.yfive.gbjs.domain.spot.entity.SortBy;
import com.yfive.gbjs.domain.spot.exception.SpotErrorStatus;
import com.yfive.gbjs.domain.tts.dto.request.TtsRequest;
import com.yfive.gbjs.domain.tts.entity.AudioFile;
import com.yfive.gbjs.domain.tts.repository.TtsRepository;
import com.yfive.gbjs.domain.tts.service.TtsService;
import com.yfive.gbjs.domain.user.entity.User;
import com.yfive.gbjs.domain.user.service.UserService;
import com.yfive.gbjs.global.error.exception.CustomException;
import com.yfive.gbjs.global.page.dto.response.PageResponse;
import com.yfive.gbjs.global.page.exception.PageErrorStatus;
import com.yfive.gbjs.global.page.mapper.PageMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotServiceImpl implements SpotService {

  private final TtsService ttsService;
  private final UserService userService;

  @Value("${openapi.secret.key}")
  private String serviceKey;

  @Value("${tourist.api.url}")
  private String spotApiUrl;

  private final ObjectMapper objectMapper;
  private final RestClient restClient;
  private final PageMapper pageMapper;
  private final AudioGuideRepository audioGuideRepository;
  private final TtsRepository ttsRepository;

  @Override
  public PageResponse<SpotResponse> getSpotsByKeywordAndCategorySortedByDistance(
      Pageable pageable,
      String keyword,
      SortBy sortBy,
      SearchBy searchBy,
      Double latitude,
      Double longitude) {

    String cat1 = "", cat2 = "", cat3 = "";

    if (searchBy != null) {
      String[] categories = mapSearchByToCategoryCodes(searchBy);
      cat1 = categories[0];
      cat2 = categories[1];
      cat3 = categories[2];
    }

    List<SpotResponse> spotResponses =
        fetchSpotListByKeyword(keyword, cat1, cat2, cat3, latitude, longitude);

    if (sortBy == SortBy.DISTANCE) {
      spotResponses.sort(
          Comparator.comparing(SpotResponse::getDistance, Comparator.nullsLast(Double::compareTo)));
    } else {
      spotResponses.sort(
          Comparator.comparing(SpotResponse::getTitle, Comparator.nullsLast(String::compareTo)));
    }

    log.info("관광지 조회 성공 - 키워드: {}, 정렬: {}, 검색유형: {}", keyword, sortBy, searchBy);
    return paginateSpotResponses(spotResponses, pageable, latitude, longitude);
  }

  private List<SpotResponse> fetchSpotListByKeyword(
      String keyword, String cat1, String cat2, String cat3, Double latitude, Double longitude) {

    String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
    UriComponentsBuilder uriBuilder =
        UriComponentsBuilder.fromUriString(spotApiUrl + "/searchKeyword2")
            .queryParam("serviceKey", serviceKey)
            .queryParam("numOfRows", 1000)
            .queryParam("pageNo", 1)
            .queryParam("MobileOS", "WEB")
            .queryParam("MobileApp", "gbjs")
            .queryParam("_type", "JSON")
            .queryParam("arrange", "O")
            .queryParam("keyword", encodedKeyword)
            .queryParam("areaCode", "35")
            .queryParam("cat1", cat1)
            .queryParam("cat2", cat2)
            .queryParam("cat3", cat3);

    String response =
        restClient.get().uri(uriBuilder.build(true).toUri()).retrieve().body(String.class);

    validateApiResponse(response);

    try {
      JsonNode root = objectMapper.readTree(response);
      JsonNode items = root.path("response").path("body").path("items").path("item");

      List<SpotResponse> spotResponses = new ArrayList<>();
      for (JsonNode item : items) {
        SpotResponse spotResponse = objectMapper.treeToValue(item, SpotResponse.class);
        spotResponses.add(spotResponse);

        if (latitude != null
            && longitude != null
            && item.get("mapy") != null
            && item.get("mapx") != null) {
          double distance =
              calculateDistance(
                  latitude, longitude, item.get("mapy").asDouble(), item.get("mapx").asDouble());
          spotResponse.setDistance(distance);
        } else {
          spotResponse.setDistance(null);
        }

        boolean ttsExist = audioGuideRepository.existsByContentId(item.get("contentid").asLong());

        spotResponse.setTtsExist(ttsExist);
      }

      return spotResponses;
    } catch (Exception e) {
      log.error("관광지 목록 파싱 실패", e);
      throw new CustomException(SpotErrorStatus.SPOT_API_ERROR);
    }
  }

  @Override
  @Transactional
  public SpotDetailResponse getSpotByContentId(
      Long contentId, Double latitude, Double longitude, Boolean isDetail) {

    UriComponentsBuilder uriBuilder =
        UriComponentsBuilder.fromUriString(spotApiUrl + "/detailCommon2")
            .queryParam("serviceKey", serviceKey)
            .queryParam("MobileOS", "WEB")
            .queryParam("MobileApp", "gbjs")
            .queryParam("_type", "JSON")
            .queryParam("contentId", contentId);

    String response =
        restClient.get().uri(uriBuilder.build(true).toUri()).retrieve().body(String.class);

    validateApiResponse(response);

    try {
      JsonNode root = objectMapper.readTree(response);
      JsonNode itemNode = root.path("response").path("body").path("items").path("item");
      if (itemNode.isArray()) {
        if (itemNode.isEmpty()) {
          throw new CustomException(SpotErrorStatus.SPOT_API_ERROR);
        }
        itemNode = itemNode.get(0);
      }

      SpotDetailResponse spotDetailResponse =
          objectMapper.treeToValue(itemNode, SpotDetailResponse.class);

      if (latitude != null
          && longitude != null
          && itemNode.get("mapy") != null
          && itemNode.get("mapx") != null) {
        double distance =
            calculateDistance(
                latitude,
                longitude,
                itemNode.get("mapy").asDouble(),
                itemNode.get("mapx").asDouble());
        spotDetailResponse.setDistance(distance);
      } else {
        spotDetailResponse.setDistance(null);
      }
      spotDetailResponse.setType(
          fetchSpotType(
              itemNode.get("contenttypeid").asText(),
              itemNode.get("cat1").asText(),
              itemNode.get("cat2").asText(),
              itemNode.get("cat3").asText()));

      List<AudioGuide> audioGuides = audioGuideRepository.findByContentId(contentId);

      if (isDetail) {
        User user = userService.getCurrentUser();
        String type;
        switch (user.getTtsSetting()) {
          case FEMALE_B -> type = "B";
          case MALE_C -> type = "C";
          case MALE_D -> type = "D";
          default -> type = "A";
        }

        List<SpotTtsResponse> spotTtsResponses =
            audioGuides.stream()
                .map(
                    guide -> {
                      AudioFile audioFile =
                          ttsRepository.findByTypeAndAudioGuideId(type, guide.getId());

                      if (audioFile == null) {
                        ttsService.convertTextToSpeech(
                            guide.getId(), user.getTtsSetting(), new TtsRequest(guide.getScript()));
                        audioFile = ttsRepository.findByTypeAndAudioGuideId(type, guide.getId());
                      }

                      return SpotTtsResponse.builder()
                          .guideId(guide.getId())
                          .title(guide.getTitle())
                          .script(guide.getScript())
                          .audioURL(audioFile.getFileUrl())
                          .build();
                    })
                .toList();

        spotDetailResponse.setTtsResponseList(spotTtsResponses);
        spotDetailResponse.setTotalTts(spotTtsResponses.size());

        log.info(
            "관광지 단일 조회 성공 - userId: {}, contentId: {}, category: {}",
            user.getId(),
            contentId,
            spotDetailResponse.getType());
      }

      return spotDetailResponse;
    } catch (Exception e) {
      log.error("관광지 정보 파싱 실패", e);
      throw new CustomException(SpotErrorStatus.SPOT_API_ERROR);
    }
  }

  private String fetchSpotType(String typeId, String cat1, String cat2, String cat3) {

    UriComponentsBuilder uriBuilder =
        UriComponentsBuilder.fromUriString(spotApiUrl + "/categoryCode2")
            .queryParam("serviceKey", serviceKey)
            .queryParam("MobileOS", "WEB")
            .queryParam("MobileApp", "gbjs")
            .queryParam("contentTypeId", typeId)
            .queryParam("cat1", cat1)
            .queryParam("cat2", cat2)
            .queryParam("cat3", cat3)
            .queryParam("_type", "JSON");

    String response =
        restClient.get().uri(uriBuilder.build(true).toUri()).retrieve().body(String.class);

    validateApiResponse(response);

    try {
      JsonNode root = objectMapper.readTree(response);
      JsonNode itemNode = root.path("response").path("body").path("items").path("item");
      if (itemNode.isArray()) {
        if (itemNode.isEmpty()) {
          throw new CustomException(SpotErrorStatus.SPOT_API_ERROR);
        }
        itemNode = itemNode.get(0);
      }
      return itemNode.get("name").asText();
    } catch (Exception e) {
      log.error("관광지 분류코드 파싱 실패", e);
      throw new CustomException(SpotErrorStatus.SPOT_API_ERROR);
    }
  }

  private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    final int R = 6371; // 지구 반지름 (km)
    double latDist = Math.toRadians(lat2 - lat1);
    double lonDist = Math.toRadians(lon2 - lon1);
    double a =
        Math.sin(latDist / 2) * Math.sin(latDist / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDist / 2)
                * Math.sin(lonDist / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }

  private void validateApiResponse(String response) {
    if (response == null || response.isBlank()) {
      log.error("빈 응답 수신");
      throw new CustomException(SpotErrorStatus.SPOT_API_ERROR);
    }

    if (response.trim().startsWith("<?xml") || response.trim().startsWith("<")) {
      throw new CustomException(SpotErrorStatus.SPOT_API_ERROR);
    }
  }

  private PageResponse<SpotResponse> paginateSpotResponses(
      List<SpotResponse> spotResponses, Pageable pageable, Double latitude, Double longitude) {

    if (pageable.getOffset() > Integer.MAX_VALUE) {
      throw new CustomException(PageErrorStatus.PAGE_NOT_FOUND);
    }

    int start = (int) pageable.getOffset();
    if (start >= spotResponses.size()) {
      throw new CustomException(PageErrorStatus.PAGE_NOT_FOUND);
    }

    int end = Math.min(start + pageable.getPageSize(), spotResponses.size());
    List<SpotResponse> pageContent = spotResponses.subList(start, end);

    pageContent.forEach(
        response -> {
          SpotDetailResponse detail =
              getSpotByContentId(response.getSpotId(), latitude, longitude, false);
          response.setType(detail.getType());
        });
    Page<SpotResponse> page = new PageImpl<>(pageContent, pageable, spotResponses.size());
    return pageMapper.toSpotPageResponse(page);
  }

  private String[] mapSearchByToCategoryCodes(SearchBy searchBy) {
    String cat1 = "";
    String cat2 = "";
    String cat3 = "";
    switch (searchBy) {
      case MONUMENT_VIEWPOINT -> {
        cat1 = "A02";
        cat2 = "A0205";
        cat3 = "A02050200";
      }
      case TOURIST_COMPLEX -> {
        cat1 = "A02";
        cat2 = "A0202";
        cat3 = "A02020200";
      }
      case HISTORIC_SITE -> {
        cat1 = "A02";
        cat2 = "A0201";
        cat3 = "A02010700";
      }
      case HANOK -> {
        cat1 = "B02";
        cat2 = "B0201";
        cat3 = "B02011600";
      }
      case PARK -> {
        cat1 = "A01";
        cat2 = "A0101";
        cat3 = "A01010100";
      }
      case FOLK_VILLAGE -> {
        cat1 = "A02";
        cat2 = "A0201";
        cat3 = "A02010600";
      }
      case CAMPING_SITE -> {
        cat1 = "A03";
        cat2 = "A0302";
        cat3 = "A03021700";
      }
      case EXHIBITION_HALL -> {
        cat1 = "A02";
        cat2 = "A0206";
        cat3 = "A02060300";
      }
      case TEMPLE -> {
        cat1 = "A02";
        cat2 = "A0201";
        cat3 = "A02010800";
      }
      case MUSEUM -> {
        cat1 = "A02";
        cat2 = "A0206";
        cat3 = "A02060100";
      }
    }
    return new String[] {cat1, cat2, cat3};
  }
}
