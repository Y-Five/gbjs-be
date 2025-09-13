/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 띠부씰 정렬 옵션 */
@Getter
@RequiredArgsConstructor
public enum SortBy {
  NUMBER("번호순"),
  RARITY("희귀도순"),
  LOCATION("지역순"),
  COLLECTED("수집순"),
  LATEST("최신순"),
  OLDEST("오래된순");

  private final String description;
}
