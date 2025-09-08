/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.config;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.yfive.gbjs.domain.guide.repository.AudioGuideRepository;
import com.yfive.gbjs.domain.guide.service.GuideService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 오디오 데이터 초기화 클래스
 *
 * <p>DB가 비어있을 때만 초기 데이터를 수집합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AudioDataInitializer {

  private final GuideService guideService;
  private final AudioGuideRepository audioGuideRepository;

  @PostConstruct
  public void init() {
    log.info("오디오 가이드 데이터 초기화를 확인합니다.");
    // DB에 데이터가 없을 때만 실행
    log.info("DB가 비어있어 초기 데이터 동기화를 시작합니다.");
    try {
      int savedCount = guideService.syncGyeongbukAudioStories();
      log.info("초기 데이터 동기화 완료. 저장된 개수: {}", savedCount);
    } catch (Exception e) {
      log.error("초기 데이터 동기화 중 오류가 발생했습니다.", e);
    }
  }
}
