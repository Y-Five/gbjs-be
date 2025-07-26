package com.yfive.gbjs.domain.seal.converter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.yfive.gbjs.domain.seal.dto.response.SealProductResponse;
import com.yfive.gbjs.domain.seal.entity.SealProduct;

@Component
public class SealProductConverter {

  public SealProductResponse.SealProductDTO toDTO(SealProduct sealProduct) {
    return SealProductResponse.SealProductDTO.builder()
        .id(sealProduct.getId())
        .name(sealProduct.getName())
        .description(sealProduct.getDescription())
        .price(sealProduct.getPrice())
        .imageUrl(sealProduct.getImageUrl())
        .build();
  }

  public SealProductResponse.SealProductListDTO toListDTO(
      List<SealProductResponse.SealProductDTO> products) {
    return SealProductResponse.SealProductListDTO.builder()
        .products(products)
        .totalCount(products.size())
        .build();
  }
}
