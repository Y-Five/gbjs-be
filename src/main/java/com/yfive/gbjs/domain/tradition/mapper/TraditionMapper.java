/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tradition.mapper;

import org.springframework.stereotype.Component;

import com.yfive.gbjs.domain.tradition.dto.response.TraditionResponse;
import com.yfive.gbjs.domain.tradition.entity.Tradition;

@Component
public class TraditionMapper {

  public TraditionResponse toTraditionResponse(Tradition tradition) {
    return TraditionResponse.builder()
        .id(tradition.getId())
        .type(tradition.getType())
        .address(tradition.getAddress())
        .name(tradition.getName())
        .description(tradition.getDescription())
        .price(tradition.getPrice())
        .imageUrl(tradition.getImageUrl())
        .build();
  }
}
