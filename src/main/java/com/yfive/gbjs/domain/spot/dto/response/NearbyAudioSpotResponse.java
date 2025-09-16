/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.spot.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyAudioSpotResponse {
  @JsonProperty("contentId")
  private Long contentId;

  private String title;

  @JsonProperty("image")
  private String imageUrl;

  @JsonProperty("hashtag")
  private String type;
}
