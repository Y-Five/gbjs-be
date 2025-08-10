/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tts.entity;

import io.swagger.v3.oas.annotations.media.Schema;

public enum Gender {
  @Schema(description = "남자")
  MALE,
  @Schema(description = "여자")
  FEMALE
}
