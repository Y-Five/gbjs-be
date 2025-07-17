/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.service;

import com.yfive.gbjs.domain.guide.dto.response.AudioStoryListResponse;
import com.yfive.gbjs.domain.guide.dto.response.GuideListResponse;

public interface GuideService {

  GuideListResponse getThemeBasedList(Integer pageNo, Integer numOfRows);

  GuideListResponse getThemeLocationBasedList(
      Double longitude, Double latitude, Integer radius, Integer pageNo, Integer numOfRows);

  GuideListResponse getThemeSearchList(String keyword, Integer pageNo, Integer numOfRows);

  AudioStoryListResponse getAudioStoryBasedList(String themeId, Integer pageNo, Integer numOfRows);

  AudioStoryListResponse getAudioStoryLocationBasedList(
      Double longitude, Double latitude, Integer radius, Integer pageNo, Integer numOfRows);

  AudioStoryListResponse getAudioStorySearchList(String keyword, Integer pageNo, Integer numOfRows);
}
