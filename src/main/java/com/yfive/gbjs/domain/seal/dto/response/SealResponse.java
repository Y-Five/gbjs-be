/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.dto.response;

import com.yfive.gbjs.domain.seal.entity.Category;
import com.yfive.gbjs.domain.seal.entity.Location;
import com.yfive.gbjs.domain.seal.entity.Seal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 띠부씰 응답 DTO
 * 띠부씰 정보를 클라이언트에 전달하기 위한 데이터 전송 객체
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SealResponse {

  private Long id;
  private String name;
  private Location location;
  private String content;
  private Category category;
  private List<String> hashtagList;

  /**
   * Seal 엔티티를 SealResponse DTO로 변환
   *
   * @param seal 띠부씰 엔티티
   * @return 띠부씰 응답 DTO
   */
  public static SealResponse of(Seal seal) {
    return SealResponse.builder()
        .id(seal.getId())
        .name(seal.getName())
        .location(seal.getLocation())
        .content(seal.getContent())
        .category(seal.getCategory())
        .hashtagList(seal.getHashtagList())
        .build();
  }
}