/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.spot.service;

import com.yfive.gbjs.domain.spot.dto.response.SpotResponse;
import com.yfive.gbjs.global.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface SpotService {

  PageResponse<SpotResponse> getSpotsByKeyword(
      Pageable pageable, String keyword, Double latitude, Double longitude);

  PageResponse<SpotResponse> getSpotsByKeywordSortedByDistance(
      Pageable pageable, String keyword, Double latitude, Double longitude);

  SpotResponse getSpotByContentId(String contentId, Double latitude, Double longitude);
}
