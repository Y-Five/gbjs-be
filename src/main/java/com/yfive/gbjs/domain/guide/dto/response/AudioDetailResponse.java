/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioDetailResponse {

  private Long id;
  private String title;
  private String audioTitle;
  private String script;
  private Integer playTime;
  private String audioUrl;
  private String imageUrl;
  private Double longitude;
  private Double latitude;
}
