/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.qdrant.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.yfive.gbjs.global.qdrant.service.DataIndexingService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataIndexingScheduler {

  private final DataIndexingService dataIndexingService;

  // 매일 00:00:00에 갱신
  @Scheduled(cron = "0 0 0 * * ?")
  public void runDailyIndexing() {

    // 컬렉션 생성 (없을 경우)
    dataIndexingService.createCollectionIfNotExists();

    // 각 타입의 데이터 색인
    dataIndexingService.indexSeals();
    dataIndexingService.indexSealSpots();
    dataIndexingService.indexSealProducts();
    dataIndexingService.indexUsers();
    dataIndexingService.indexSpotsFromApi();
    dataIndexingService.indexFestivalsFromApi();
  }
}
