/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.page.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.yfive.gbjs.domain.festival.dto.response.FestivalResponse;
import com.yfive.gbjs.domain.spot.dto.response.SpotResponse;
import com.yfive.gbjs.domain.tradition.dto.response.TraditionResponse;
import com.yfive.gbjs.global.page.dto.response.PageResponse;

@Component
public class PageMapper {

  private <T> PageResponse<T> toPageResponse(Page<T> page) {
    return PageResponse.<T>builder()
        .content(page.getContent())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .pageNum(page.getNumber())
        .pageSize(page.getSize())
        .last(page.isLast())
        .first(page.isFirst())
        .build();
  }

  public PageResponse<TraditionResponse> toTraditionPageResponse(Page<TraditionResponse> page) {
    return toPageResponse(page);
  }

  public PageResponse<FestivalResponse> toFestivalPageResponse(Page<FestivalResponse> page) {
    return toPageResponse(page);
  }

  public PageResponse<SpotResponse> toSpotPageResponse(Page<SpotResponse> page) {
    return toPageResponse(page);
  }
}
