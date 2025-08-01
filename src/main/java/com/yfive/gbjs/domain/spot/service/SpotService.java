/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.spot.service;

import org.springframework.data.domain.Pageable;

import com.yfive.gbjs.domain.spot.dto.response.SpotPageResponse;
import com.yfive.gbjs.domain.spot.dto.response.SpotResponse;

public interface SpotService {

  SpotPageResponse getSpotsByKeyword(
      String keyword, Pageable pageable, String sortBy, Double longitude, Double latitude);

  SpotResponse getSpotByContentId(Long contentId, Double longitude, Double latitude);
}
