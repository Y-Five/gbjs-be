/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.festival.service;

import com.yfive.gbjs.domain.festival.dto.response.FestivalListResponse;

public interface FestivalService {

  FestivalListResponse getFestivalsByRegion(String region, Integer startIndex, Integer pageSize);

  Integer getSiGunGuCode(String region);
}
