/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.qdrant.scheduler;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.yfive.gbjs.GbjsApplication;
import com.yfive.gbjs.global.qdrant.service.DataIndexingService;

/**
 * Qdrant 데이터 색인 작업을 수동으로 실행하기 위한 테스트 클래스입니다. <br>
 * 이 클래스의 테스트들은 `manual` 태그로 분류되어 자동 빌드에서 제외됩니다. <br>
 */
@Disabled("수동 실행 전용")
@SpringBootTest(classes = GbjsApplication.class)
@Tag("manual")
@TestMethodOrder(MethodOrderer.MethodName.class)
class TestQdrantIndexerRunner {

  @Autowired private DataIndexingService dataIndexingService;

  /** Qdrant 컬렉션이 존재하지 않을 경우 새로 생성합니다. */
  @Test
  void createCollectionIfNotExists() {
    dataIndexingService.createCollectionIfNotExists();
  }

  /** 모든 종류의 데이터를 Qdrant에 색인합니다. 외부 API 호출이 발생할 수 있으니 사용에 주의하세요. */
  @Test
  void run02_indexAllData() {
    indexSeals();
    indexSealSpots();
    indexSealProducts();
    indexUsers();
    indexSpotsFromApi();
    indexFestivalsFromApi();
    indexSealCollectingGuide();
  }

  @Test
  void indexSeals() {
    dataIndexingService.indexSeals();
  }

  @Test
  void indexSealSpots() {
    dataIndexingService.indexSealSpots();
  }

  @Test
  void indexSealProducts() {
    dataIndexingService.indexSealProducts();
  }

  @Test
  void indexUsers() {
    dataIndexingService.indexUsers();
  }

  @Test
  void indexSpotsFromApi() {
    dataIndexingService.indexSpotsFromApi();
  }

  @Test
  void indexFestivalsFromApi() {
    dataIndexingService.indexFestivalsFromApi();
  }

  @Test
  void indexSealCollectingGuide() {
    dataIndexingService.indexSealCollectingGuide();
  }
}
