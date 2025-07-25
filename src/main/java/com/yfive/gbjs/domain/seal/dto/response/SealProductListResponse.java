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
public class SealProductListResponse {

  private List<SealProductResponse> products;
  private int totalCount;

  public static SealProductListResponse of(List<SealProductResponse> products) {
    return SealProductListResponse.builder()
        .products(products)
        .totalCount(products.size())
        .build();
  }
}