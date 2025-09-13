/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "코스 정렬 기준")
@Getter
@RequiredArgsConstructor
public enum CourseSortBy {
  @Schema(description = "최신순 (저장 시점 기준)")
  LATEST("최신순"),

  @Schema(description = "오래된순 (저장 시점 기준)")
  OLDEST("오래된순");

  private final String description;
}
