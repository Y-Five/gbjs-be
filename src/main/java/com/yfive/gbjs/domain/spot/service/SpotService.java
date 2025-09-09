/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.spot.service;

import org.springframework.data.domain.Pageable;

import com.yfive.gbjs.domain.spot.dto.response.SpotDetailResponse;
import com.yfive.gbjs.domain.spot.dto.response.SpotResponse;
import com.yfive.gbjs.domain.spot.entity.SearchBy;
import com.yfive.gbjs.domain.spot.entity.SortBy;
import com.yfive.gbjs.global.page.dto.response.PageResponse;

public interface SpotService {

  PageResponse<SpotResponse> getSpotsByKeywordAndCategorySortedByDistance(
      Pageable pageable,
      String keyword,
      SortBy sortBy,
      SearchBy searchBy,
      Double latitude,
      Double longitude);

  SpotDetailResponse getSpotByContentId(
      Long contentId, Double latitude, Double longitude, Boolean isDetail);
}
