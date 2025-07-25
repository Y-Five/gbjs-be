/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.dto.response;

import com.yfive.gbjs.domain.seal.entity.SealProduct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 띠부씰 상품 응답 DTO
 * 띠부씰 관련 상품 정보를 전달하기 위한 데이터 전송 객체
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SealProductResponse {

  private Long id;
  private String name;
  private String description;
  private Long price;
  private String imageUrl;

  /**
   * SealProduct 엔티티를 SealProductResponse DTO로 변환
   *
   * @param sealProduct 띠부씰 상품 엔티티
   * @return 띠부씰 상품 응답 DTO
   */
  public static SealProductResponse of(SealProduct sealProduct) {
    return SealProductResponse.builder()
        .id(sealProduct.getId())
        .name(sealProduct.getName())
        .description(sealProduct.getDescription())
        .price(sealProduct.getPrice())
        .imageUrl(sealProduct.getImageUrl())
        .build();
  }
}