/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.config;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.yfive.gbjs.domain.guide.repository.AudioGuideRepository;
import com.yfive.gbjs.domain.guide.service.GuideService;

import lombok.RequiredArgsConstructor;

/**
 * 오디오 데이터 초기화 클래스
 *
 * <p>DB가 비어있을 때만 초기 데이터를 수집합니다.
 */
@Component
@RequiredArgsConstructor
public class AudioDataInitializer {

  private final GuideService guideService;
  private final AudioGuideRepository audioGuideRepository;

  @PostConstruct
  public void init() {
    // DB에 데이터가 없을 때만 실행
    if (audioGuideRepository.count() == 0) {

      try {
        int savedCount = guideService.syncGyeongbukAudioStories();
      } catch (Exception e) {
      }

    } else {
    }
  }
}
