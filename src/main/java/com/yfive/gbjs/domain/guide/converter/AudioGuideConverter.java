/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.converter;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.yfive.gbjs.domain.guide.dto.response.AudioDetailResponse;
import com.yfive.gbjs.domain.guide.entity.AudioGuide;

@Component
public class AudioGuideConverter {

  public AudioDetailResponse toAudioDetailResponse(AudioGuide audioGuide) {
    if (audioGuide == null) {
      return null;
    }

    return AudioDetailResponse.builder()
        .id(audioGuide.getId())
        .title(audioGuide.getTitle())
        .audioTitle(audioGuide.getAudioTitle())
        .script(audioGuide.getScript())
        .playTime(audioGuide.getPlayTime())
        .audioUrl(audioGuide.getAudioUrl())
        .imageUrl(audioGuide.getImageUrl())
        .longitude(parseDouble(audioGuide.getLongitude()))
        .latitude(parseDouble(audioGuide.getLatitude()))
        .build();
  }

  public List<AudioDetailResponse> toAudioDetailResponseList(List<AudioGuide> audioGuides) {
    if (audioGuides == null) {
      return List.of();
    }

    return audioGuides.stream().map(this::toAudioDetailResponse).collect(Collectors.toList());
  }

  private Double parseDouble(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
