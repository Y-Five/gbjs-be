/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "띠부씰 희귀도")
public enum Rarity {
  @Schema(description = "흔함 (파란색)")
  BLUE,
  @Schema(description = "보통 (초록색)")
  GREEN,
  @Schema(description = "희귀 (빨간색)")
  RED
}