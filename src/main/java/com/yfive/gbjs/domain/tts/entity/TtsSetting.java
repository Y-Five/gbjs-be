/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tts.entity;

import io.swagger.v3.oas.annotations.media.Schema;

public enum TtsSetting {
  @Schema(description = "여자A")
  FEMALE_A,
  @Schema(description = "여자B")
  FEMALE_B,
  @Schema(description = "남자C")
  MALE_C,
  @Schema(description = "남자D")
  MALE_D
}
