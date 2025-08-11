/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.yfive.gbjs.GbjsApplication;
import com.yfive.gbjs.domain.guide.service.GuideService;

public class AudioGuideSync {
  public static void main(String[] args) {
    // Spring 컨텍스트 시작
    ConfigurableApplicationContext context = SpringApplication.run(GbjsApplication.class, args);

    // GuideService 빈 가져오기
    GuideService guideService = context.getBean(GuideService.class);

    // 동기화 실행
    System.out.println("========== 경북 오디오 데이터 초기 수집 시작 ==========");
    int savedCount = guideService.syncGyeongbukAudioStories();
    System.out.println("========== 수집 완료: " + savedCount + "개 저장 ==========");

    // 컨텍스트 종료
    context.close();
  }
}
