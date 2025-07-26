package com.yfive.gbjs.domain.seal.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class SealProductResponse {

  @Builder
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(title = "SealProduct : 띠부씰 상품 DTO")
  public static class SealProductDTO {
    @Schema(description = "상품 ID", example = "1")
    private Long id;

    @Schema(description = "상품명", example = "경북 띠부씰 세트")
    private String name;

    @Schema(description = "상품 설명", example = "경북 지역 띠부씰 5종 세트")
    private String description;

    @Schema(description = "가격", example = "스탬프3개")
    private String price;

    @Schema(description = "이미지 URL", example = "https://example.com/seal-product.jpg")
    private String imageUrl;
  }

  @Builder
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(title = "SealProductList : 띠부씰 상품 목록 DTO")
  public static class SealProductListDTO {
    @Schema(description = "상품 목록")
    private List<SealProductDTO> products;

    @Schema(description = "전체 상품 수", example = "5")
    private int totalCount;
  }
}
