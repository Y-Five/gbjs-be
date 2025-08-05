/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tradition.entity;

import io.swagger.v3.oas.annotations.media.Schema;

public enum TraditionType {
  @Schema(description = "문화체험")
  ACTIVITY,
  @Schema(description = "특산품")
  SPECIALTIES
}
