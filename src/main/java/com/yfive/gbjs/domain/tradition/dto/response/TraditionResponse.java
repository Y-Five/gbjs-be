/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tradition.dto.response;

import com.yfive.gbjs.domain.tradition.entity.TraditionType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "TraditionResponse DTO", description = "경상북도 전통문화 응답 반환")
public class TraditionResponse {

  @Schema(description = "전통문화 식별자", example = "1")
  private Long traditionId;

  @Schema(description = "전통문화 이미지 URL")
  private String imageUrl;

  @Schema(description = "전통문화 위치", example = "경상북도 김천시")
  private String address;

  @Schema(description = "전통문화 이름", example = "포도")
  private String name;

  @Schema(description = "전통문화 설명", example = "김천은 토양에 게르마늄 함량이 높아 포도의 저장성이 좋고 당도가 높으며... ")
  private String description;

  @Schema(description = "관련 사이트 URL")
  private String redirectUrl;

  @Schema(description = "전통문화 분류", example = "SPEICIALTIES")
  private TraditionType type;

  @Schema(description = "특산물 가격", example = "10000")
  private Long price;
}
