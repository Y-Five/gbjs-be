/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSealListResponse {

  private List<UserSealResponse> seals;
  private int totalCount;
  private int collectedCount;

  public static UserSealListResponse of(List<UserSealResponse> seals) {
    int collectedCount = (int) seals.stream()
        .filter(UserSealResponse::isCollected)
        .count();
        
    return UserSealListResponse.builder()
        .seals(seals)
        .totalCount(seals.size())
        .collectedCount(collectedCount)
        .build();
  }
}