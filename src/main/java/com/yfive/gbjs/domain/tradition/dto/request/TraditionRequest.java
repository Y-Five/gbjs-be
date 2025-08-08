/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tradition.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "TraditionRequest DTO", description = "전통문화 생성을 위한 데이터 전송")
public class TraditionRequest {

  @NotBlank(message = "지역 항목은 필수입니다.")
  @Schema(description = "전통문화 지역", example = "경상북도 김천시")
  private String address;

  @NotBlank(message = "이름 항목은 필수입니다.")
  @Schema(description = "전통문화 이름", example = "포도")
  private String name;

  @NotBlank(message = "설명 항목은 필수입니다.")
  @Schema(description = "전통문화 설명", example = "김천은 토양에 게르마늄 함량이 높아 포도의 저장성이 좋고 당도가 높으며... ")
  private String description;

  @NotNull(message = "가격 항목은 필수입니다.")
  @Positive(message = "가격은 양수여야 합니다.")
  @Schema(description = "특산물 가격", example = "10000")
  private Long price;
}
