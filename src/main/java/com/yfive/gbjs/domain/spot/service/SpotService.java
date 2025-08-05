/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.spot.service;

import org.springframework.data.domain.Pageable;

import com.yfive.gbjs.domain.spot.dto.response.SpotResponse;
import com.yfive.gbjs.global.common.response.PageResponse;

public interface SpotService {

  PageResponse<SpotResponse> getSpotsByKeyword(
      String keyword, Pageable pageable, String sortBy, Double longitude, Double latitude);

  SpotResponse getSpotByContentId(Long contentId, Double longitude, Double latitude);
}
