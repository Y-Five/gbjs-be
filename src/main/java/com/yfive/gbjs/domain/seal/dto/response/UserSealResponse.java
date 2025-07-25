/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.dto.response;

import com.yfive.gbjs.domain.seal.entity.Category;
import com.yfive.gbjs.domain.seal.entity.Location;
import com.yfive.gbjs.domain.seal.entity.Seal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 띠부씰 수집 현황 응답 DTO
 * 특정 사용자의 띠부씰 수집 여부와 수집 시간 정보를 포함
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSealResponse {

  private Long id;
  private String name;
  private Location location;
  private String content;
  private Category category;
  private List<String> hashtagList;
  private boolean collected;
  private LocalDateTime collectedAt;

  /**
   * Seal 엔티티와 수집 정보를 UserSealResponse DTO로 변환
   *
   * @param seal 띠부씰 엔티티
   * @param collected 수집 여부
   * @param collectedAt 수집 시간
   * @return 사용자 띠부씰 응답 DTO
   */
  public static UserSealResponse of(Seal seal, boolean collected, LocalDateTime collectedAt) {
    return UserSealResponse.builder()
        .id(seal.getId())
        .name(seal.getName())
        .location(seal.getLocation())
        .content(seal.getContent())
        .category(seal.getCategory())
        .hashtagList(seal.getHashtagList())
        .collected(collected)
        .collectedAt(collectedAt)
        .build();
  }
}