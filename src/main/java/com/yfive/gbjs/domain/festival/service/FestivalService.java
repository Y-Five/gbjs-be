/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.festival.service;

import org.springframework.data.domain.Pageable;

import com.yfive.gbjs.domain.festival.dto.response.FestivalDetailResponse;
import com.yfive.gbjs.domain.festival.dto.response.FestivalResponse;
import com.yfive.gbjs.global.page.dto.response.PageResponse;

public interface FestivalService {

  PageResponse<FestivalResponse> getFestivalsByRegion(String region, Pageable pageable);

  FestivalDetailResponse getFestivalById(String id);
}
