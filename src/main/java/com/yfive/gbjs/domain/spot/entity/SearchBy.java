/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.spot.entity;

import io.swagger.v3.oas.annotations.media.Schema;

public enum SearchBy {
  @Schema(description = "기념탑/기념비/전망대")
  MONUMENT_VIEWPOINT,
  @Schema(description = "관광단지")
  TOURIST_COMPLEX,
  @Schema(description = "유적지/사적지")
  HISTORIC_SITE,
  @Schema(description = "한옥")
  HANOK,
  @Schema(description = "국립공원")
  PARK,
  @Schema(description = "민속마을")
  FOLK_VILLAGE,
  @Schema(description = "야영장,오토캠핑장")
  CAMPING_SITE,
  @Schema(description = "전시관")
  EXHIBITION_HALL,
  @Schema(description = "사찰")
  TEMPLE,
  @Schema(description = "박물관")
  MUSEUM,
}
