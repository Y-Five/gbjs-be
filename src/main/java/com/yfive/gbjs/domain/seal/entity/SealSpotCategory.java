/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.entity;

import io.swagger.v3.oas.annotations.media.Schema;

public enum SealSpotCategory {
  @Schema(description = "자연환경")
  NATURE,
  @Schema(description = "야경 명소")
  NIGHTSCAPE,
  @Schema(description = "힐링 명소")
  HEALING,
  @Schema(description = "유명 관광지")
  ATTRACTION,
  @Schema(description = "액티비티")
  ACTIVITY
}
