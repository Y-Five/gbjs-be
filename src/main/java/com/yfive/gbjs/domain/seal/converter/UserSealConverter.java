/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.converter;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.yfive.gbjs.domain.seal.dto.response.UserSealResponse;
import com.yfive.gbjs.domain.seal.entity.Seal;

@Component
public class UserSealConverter {

  public UserSealResponse.UserSealDTO toDTO(
      Seal seal, boolean collected, LocalDateTime collectedAt) {
    return UserSealResponse.UserSealDTO.builder()
        .id(seal.getId())
        .number(seal.getNumber())
        .spotName(seal.getSpotName())
        .locationName(seal.getLocationName())
        .location(seal.getLocation())
        .content(seal.getContent())
        .rarity(seal.getRarity())
        .frontImageUrl(seal.getFrontImageUrl())
        .backImageUrl(seal.getBackImageUrl())
        .collected(collected)
        .collectedAt(collectedAt)
        .build();
  }

  public UserSealResponse.UserSealListDTO toListDTO(List<UserSealResponse.UserSealDTO> seals) {
    long collectedCount = seals.stream().filter(UserSealResponse.UserSealDTO::isCollected).count();

    return UserSealResponse.UserSealListDTO.builder()
        .totalCount(seals.size())
        .collectedCount((int) collectedCount)
        .seals(seals)
        .build();
  }

  public UserSealResponse.NearbySealDTO toNearbyDTO(
      Seal seal, boolean collected, LocalDateTime collectedAt, Integer distance) {
    return UserSealResponse.NearbySealDTO.builder()
        .id(seal.getId())
        .number(seal.getNumber())
        .spotName(seal.getSpotName())
        .locationName(seal.getLocationName())
        .location(seal.getLocation())
        .content(seal.getContent())
        .rarity(seal.getRarity())
        .frontImageUrl(seal.getFrontImageUrl())
        .backImageUrl(seal.getBackImageUrl())
        .collected(collected)
        .collectedAt(collectedAt)
        .distance(distance)
        .build();
  }
}
