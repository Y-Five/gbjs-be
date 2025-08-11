/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.page.mapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import com.yfive.gbjs.domain.festival.dto.response.FestivalResponse;
import com.yfive.gbjs.domain.spot.dto.response.SpotResponse;
import com.yfive.gbjs.domain.tradition.dto.response.TraditionResponse;
import com.yfive.gbjs.global.common.response.PageResponse;

@Component
public class PageMapper {

  public PageResponse<TraditionResponse> toTraditionPageResponse(Page<TraditionResponse> page) {
    return PageResponse.<TraditionResponse>builder()
        .content(page.getContent())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .pageNum(page.getNumber())
        .pageSize(page.getSize())
        .last(page.isLast())
        .first(page.isFirst())
        .build();
  }

  public PageResponse<FestivalResponse> toFestivalPageResponse(PageImpl<FestivalResponse> page) {
    return PageResponse.<FestivalResponse>builder()
        .content(page.getContent())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .pageNum(page.getNumber())
        .pageSize(page.getSize())
        .last(page.isLast())
        .first(page.isFirst())
        .build();
  }

  public PageResponse<SpotResponse> toSpotPageResponse(PageImpl<SpotResponse> page) {
    return PageResponse.<SpotResponse>builder()
        .content(page.getContent())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .pageNum(page.getNumber())
        .pageSize(page.getSize())
        .last(page.isLast())
        .first(page.isFirst())
        .build();
  }
}
