/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "추천 코스 타입")
public enum RecommendationType {
  @Schema(description = "테마별 코스")
  THEME,

  @Schema(description = "행사별 코스")
  FESTIVAL
}
