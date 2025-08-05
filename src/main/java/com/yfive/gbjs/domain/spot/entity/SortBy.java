/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.spot.entity;

import io.swagger.v3.oas.annotations.media.Schema;

public enum SortBy {
  @Schema(description = "거리순")
  DISTANCE,
  @Schema(description = "가나다순")
  ABC
}
