/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.yfive.gbjs.domain.guide.service.GuideService;

import lombok.RequiredArgsConstructor;

/**
 * 오디오 데이터 스케줄러
 *
 * <p>주기적으로 경북 지역 오디오 데이터를 DB에 동기화합니다. application.properties에서 audio.sync.scheduler.enabled=true 설정
 * 시 활성화됩니다.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "audio.sync.scheduler.enabled", havingValue = "true")
public class AudioDataScheduler {

  private final GuideService guideService;

  /** 매일 새벽 2시에 경북 지역 오디오 데이터를 동기화합니다. */
  @Scheduled(cron = "0 0 2 * * ?")
  public void syncAudioData() {

    try {
      int savedCount = guideService.syncGyeongbukAudioStories();
    } catch (Exception e) {
    }
  }
}
