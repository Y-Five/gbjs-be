/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.converter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.yfive.gbjs.domain.seal.dto.response.SealResponse;
import com.yfive.gbjs.domain.seal.entity.Seal;

@Component
public class SealConverter {

  public SealResponse.SealDTO toDTO(Seal seal) {
    return SealResponse.SealDTO.builder()
        .id(seal.getId())
        .number(seal.getNumber())
        .spotName(seal.getSpotName())
        .locationName(seal.getLocationName())
        .location(seal.getLocation())
        .content(seal.getContent())
        .rarity(seal.getRarity())
        .frontImageUrl(seal.getFrontImageUrl())
        .backImageUrl(seal.getBackImageUrl())
        .build();
  }

  public SealResponse.SealListDTO toListDTO(List<SealResponse.SealDTO> seals) {
    return SealResponse.SealListDTO.builder().seals(seals).totalCount(seals.size()).build();
  }
}
