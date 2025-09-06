/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.yfive.gbjs.domain.seal.dto.response.PopularSealSpotResponse;
import com.yfive.gbjs.domain.seal.dto.response.SealResponse;
import com.yfive.gbjs.domain.seal.entity.Seal;
import com.yfive.gbjs.domain.seal.entity.SealSpot;

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
    return SealResponse.SealListDTO.builder().totalCount(seals.size()).seals(seals).build();
  }

  public PopularSealSpotResponse toPopularSealSpotDTO(SealSpot sealSpot) {
    List<String> hashtags = Collections.emptyList();
    if (sealSpot.getHashtag() != null && !sealSpot.getHashtag().isEmpty()) {
      hashtags =
          Arrays.stream(sealSpot.getHashtag().split("#"))
              .filter(s -> !s.isEmpty())
              .collect(Collectors.toList());
    }

    return PopularSealSpotResponse.builder()
        .spotId(sealSpot.getSpotId())
        .name(sealSpot.getName())
        .imageUrl(sealSpot.getImageUrl())
        .hashtag(hashtags)
        .build();
  }
}
