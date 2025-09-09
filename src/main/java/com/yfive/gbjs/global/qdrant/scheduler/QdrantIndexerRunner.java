/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.qdrant.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.yfive.gbjs.global.qdrant.service.DataIndexingService;

import io.qdrant.client.QdrantClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile("!test")
@Component
@RequiredArgsConstructor
@Slf4j
public class QdrantIndexerRunner implements CommandLineRunner {

  private final DataIndexingService dataIndexingService;
  private final QdrantClient qdrantClient;

  @Value("${spring.ai.vectorstore.qdrant.collection-name}")
  private String collectionName;

  @Override
  public void run(String... args) throws Exception {
    log.warn("--- TEMPORARY RUNNER: STARTING QDRANT DATA RESET ---");

    // 1. 컬렉션 삭제 (주석 처리됨)
    //    try {
    //      qdrantClient.deleteCollectionAsync(collectionName).get();
    //      log.info("✅ Successfully deleted Qdrant collection: {}", collectionName);
    //      Thread.sleep(1000);
    //    } catch (Exception e) {
    //      log.warn("Could not delete collection '{}' (it may not exist).", collectionName);
    //    }

    // 2. 컬렉션 생성 (주석 처리됨)
    //    dataIndexingService.createCollectionIfNotExists();

    // 3. 모든 데이터 재색인 (indexSpotsFromApi만 실행)
    //    dataIndexingService.indexSeals();
    //    dataIndexingService.indexSealSpots();
    //    dataIndexingService.indexSealProducts();
    //    dataIndexingService.indexUsers();
    dataIndexingService.indexSpotsFromApi();
    //    dataIndexingService.indexFestivalsFromApi();

    log.warn("--- TEMPORARY RUNNER: FINISHED QDRANT DATA RESET ---");

    // 4. 임시 검증 코드 (주석 처리됨)
    //     verifyData();
  }

  //    private void verifyData() {
  //      try {
  //        log.info("--- VERIFICATION STEP ---");
  //        CollectionInfo collectionInfo =
  // qdrantClient.getCollectionInfoAsync(collectionName).get();
  //        long pointCount = collectionInfo.getPointsCount();
  //        log.info(
  //            "✅ Verification complete. Found {} points in collection '{}'.",
  //            pointCount,
  //            collectionName);
  //        if (pointCount == 0) {
  //          log.warn(
  //              "⚠️  Warning: 0 points found. This might mean the source database tables are
  //   empty.");
  //        }
  //      } catch (Exception e) {
  //        log.error("Error during verification step.", e);
  //      }
  //    }
}
