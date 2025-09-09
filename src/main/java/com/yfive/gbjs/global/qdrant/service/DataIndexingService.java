/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.qdrant.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yfive.gbjs.domain.festival.dto.response.FestivalDetailResponse;
import com.yfive.gbjs.domain.festival.dto.response.FestivalResponse;
import com.yfive.gbjs.domain.festival.service.FestivalService;
import com.yfive.gbjs.domain.seal.entity.Seal;
import com.yfive.gbjs.domain.seal.entity.SealProduct;
import com.yfive.gbjs.domain.seal.entity.SealSpot;
import com.yfive.gbjs.domain.seal.repository.SealProductRepository;
import com.yfive.gbjs.domain.seal.repository.SealRepository;
import com.yfive.gbjs.domain.seal.repository.SealSpotRepository;
import com.yfive.gbjs.domain.spot.dto.response.SpotDetailResponse;
import com.yfive.gbjs.domain.spot.dto.response.SpotResponse;
import com.yfive.gbjs.domain.spot.service.SpotService;
import com.yfive.gbjs.domain.user.entity.User;
import com.yfive.gbjs.domain.user.repository.UserRepository;
import com.yfive.gbjs.global.page.dto.response.PageResponse;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.CreateCollection;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DataIndexingService {

  private final VectorStore vectorStore;
  private final SealRepository sealRepository;
  private final SealSpotRepository sealSpotRepository;
  private final SealProductRepository sealProductRepository;
  private final UserRepository userRepository;
  private final SpotService spotService;
  private final FestivalService festivalService;
  private final QdrantClient qdrantClient;

  @Value("${spring.ai.vectorstore.qdrant.collection-name}")
  private String collectionName;

  public void createCollectionIfNotExists() {
    try {
      boolean collectionExists = qdrantClient.listCollectionsAsync().get().contains(collectionName);
      if (!collectionExists) {
        qdrantClient
            .createCollectionAsync(
                CreateCollection.newBuilder()
                    .setCollectionName(collectionName)
                    .setVectorsConfig(
                        io.qdrant.client.grpc.Collections.VectorsConfig.newBuilder()
                            .setParams(
                                VectorParams.newBuilder()
                                    .setSize(1536) // OpenAI text-embedding-ada-002
                                    .setDistance(Distance.Cosine)
                                    .build())
                            .build())
                    .build())
            .get();
      }
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Transactional
  public void indexSeals() {
    List<Seal> seals = sealRepository.findAll();
    List<Document> documents =
        seals.stream()
            .map(
                seal -> {
                  String searchableContent =
                      "씰 번호: "
                          + seal.getNumber()
                          + ", 씰 ID: "
                          + seal.getId()
                          + ", 씰 이름: "
                          + seal.getSpotName()
                          + ", 지역명: "
                          + seal.getLocationName()
                          + ", 시: "
                          + seal.getContent()
                          + ", 희귀도: "
                          + seal.getRarity().name()
                          + ", 위치: "
                          + seal.getLocation().name()
                          + ", 씰 관광지 ID: "
                          + (seal.getSealSpot() != null ? seal.getSealSpot().getId() : "없음");

                  UUID documentId =
                      UUID.nameUUIDFromBytes(
                          ("seal-" + seal.getId()).getBytes(StandardCharsets.UTF_8));
                  return new Document(
                      documentId.toString(),
                      searchableContent,
                      Map.of("entity_type", "seal", "sealId", seal.getId()));
                })
            .collect(Collectors.toList());
    vectorStore.add(documents);
  }

  @Transactional
  public void indexSealSpots() {
    List<SealSpot> sealSpots = sealSpotRepository.findAll();
    List<Document> documents =
        sealSpots.stream()
            .map(
                spot -> {
                  String searchableContent =
                      "씰 관광지 이름: "
                          + spot.getName()
                          + ", 씰 관광지 ID: "
                          + spot.getId()
                          + ", 설명: "
                          + spot.getDescription()
                          + ", 위치: "
                          + spot.getLocation().name()
                          + ", 주소: "
                          + spot.getAddr1()
                          + ", 카테고리: "
                          + (spot.getCategory() != null ? spot.getCategory().name() : "없음")
                          + ", 오디오 가이드 ID: "
                          + (spot.getAudioGuide() != null ? spot.getAudioGuide().getId() : "없음")
                          + ", 해시태그: "
                          + (spot.getHashtag() != null ? spot.getHashtag() : "없음");

                  UUID documentId =
                      UUID.nameUUIDFromBytes(
                          ("seal_spot-" + spot.getId()).getBytes(StandardCharsets.UTF_8));
                  return new Document(
                      documentId.toString(),
                      searchableContent,
                      Map.of(
                          "entity_type",
                          "seal_spot",
                          "sealSpotId",
                          spot.getId(),
                          "type",
                          "spot",
                          "location",
                          spot.getLocation().name(),
                          "spotId",
                          spot.getSpotId() != null ? spot.getSpotId() : "없음"));
                })
            .collect(Collectors.toList());
    vectorStore.add(documents);
  }

  @Transactional
  public void indexSealProducts() {
    List<SealProduct> sealProducts = sealProductRepository.findAll();
    List<Document> documents =
        sealProducts.stream()
            .map(
                product -> {
                  String searchableContent =
                      "씰 상품 이름: "
                          + product.getName()
                          + ", 씰 상품 ID: "
                          + product.getId()
                          + ", 설명: "
                          + product.getDescription()
                          + ", 가격: "
                          + product.getPrice();

                  UUID documentId =
                      UUID.nameUUIDFromBytes(
                          ("seal_product-" + product.getId()).getBytes(StandardCharsets.UTF_8));
                  return new Document(
                      documentId.toString(),
                      searchableContent,
                      Map.of("entity_type", "seal_product", "sealProductId", product.getId()));
                })
            .collect(Collectors.toList());
    vectorStore.add(documents);
  }

  @Transactional
  public void indexUsers() {
    List<User> users = userRepository.findAll();
    List<Document> documents =
        users.stream()
            .map(
                user -> {
                  String searchableContent =
                      "사용자 ID: " + user.getId() + ", 닉네임: " + user.getNickname();

                  UUID documentId =
                      UUID.nameUUIDFromBytes(
                          ("user-" + user.getId()).getBytes(StandardCharsets.UTF_8));
                  return new Document(
                      documentId.toString(),
                      searchableContent,
                      Map.of("entity_type", "user", "userId", user.getId()));
                })
            .collect(Collectors.toList());
    vectorStore.add(documents);
  }

  //  @Transactional
  //  public void indexSpotsFromApi() {
  //    log.info("관광지 정보 색인 시작");
  //    List<String> regions =
  //        Arrays.asList(
  //            "경산", "경주", "고령", "구미", "김천", "문경", "봉화", "상주", "성주", "안동", "영덕", "영양", "영주", "영천",
  //            "예천", "울릉", "울진", "의성", "청도", "청송", "칠곡", "포항");
  //
  //    List<Document> allDocuments = new ArrayList<>();
  //
  //    for (String region : regions) {
  //      int pageNumber = 0;
  //      int pageSize = 100;
  //
  //      while (true) {
  //        try {
  //          Pageable pageable = PageRequest.of(pageNumber, pageSize);
  //          String keyword = region;
  //          PageResponse<SpotResponse> spotPage =
  //              spotService.getSpotsByKeyword(pageable, keyword, null, null);
  //
  //          List<Document> documentsOnPage =
  //              spotPage.getContent().stream()
  //                  .map(
  //                      spot -> {
  //                        try {
  //                          SpotDetailResponse detail =
  //                              spotService.getSpotByContentId(spot.getSpotId(), null, null);
  //                          if (detail == null) return null;
  //
  //                          String title = detail.getTitle() != null ? detail.getTitle() : "";
  //                          String overview =
  //                              detail.getOverview() != null ? detail.getOverview() : "";
  //                          String searchableContent = "관광지 이름: " + title + ", 설명: " + overview;
  //
  //                          UUID documentId =
  //                              UUID.nameUUIDFromBytes(
  //                                  ("spot-" +
  // detail.getSpotId()).getBytes(StandardCharsets.UTF_8));
  //                          return new Document(
  //                              documentId.toString(),
  //                              searchableContent,
  //                              Map.of(
  //                                  "type",
  //                                  "spot",
  //                                  "contentId",
  //                                  detail.getSpotId(),
  //                                  "name",
  //                                  detail.getTitle(),
  //                                  "addr1",
  //                                  detail.getAddress(),
  //                                  "category",
  //                                  detail.getType()));
  //                        } catch (Exception e) {
  //                          log.error("관광지 상세 정보 조회 실패 - spotId: {}", spot.getSpotId(), e);
  //                          return null;
  //                        }
  //                      })
  //                  .filter(Objects::nonNull)
  //                  .collect(Collectors.toList());
  //
  //          allDocuments.addAll(documentsOnPage);
  //
  //          if (spotPage.getLast() || spotPage.getContent().isEmpty()) {
  //            log.info(
  //                "{} 관광지 {}개 처리 완료", region, (pageNumber * pageSize) +
  // spotPage.getContent().size());
  //            break;
  //          }
  //
  //          pageNumber++;
  //
  //        } catch (Exception e) {
  //          log.error("{} 지역의 {} 페이지 색인 중 오류 발생", region, pageNumber, e);
  //          break;
  //        }
  //      }
  //    }
  //
  //    if (!allDocuments.isEmpty()) {
  //      vectorStore.add(allDocuments);
  //    }
  //    log.info("관광지 정보 총 {}개 색인 완료", allDocuments.size());
  //  }

  @Transactional
  public void indexSpotsFromApi() {
    log.info("관광지 정보 색인 시작");
    List<String> regions =
        Arrays.asList(
            "경산", "경주", "고령", "구미", "김천", "문경", "봉화", "상주", "성주", "안동", "영덕", "영양", "영주", "영천",
            "예천", "울릉", "울진", "의성", "청도", "청송", "칠곡", "포항");

    List<Document> allDocuments = new ArrayList<>();

    for (String region : regions) {
      int pageNumber = 0;
      int pageSize = 100;

      while (true) {
        try {
          Pageable pageable = PageRequest.of(pageNumber, pageSize);
          String keyword = region;
          PageResponse<SpotResponse> spotPage =
              spotService.getSpotsByKeyword(pageable, keyword, null, null);

          List<Document> documentsOnPage =
              spotPage.getContent().stream()
                  .map(
                      spot -> {
                        try {
                          SpotDetailResponse detail =
                              spotService.getSpotByContentId(spot.getSpotId(), null, null);
                          if (detail == null) return null;

                          String title = detail.getTitle() != null ? detail.getTitle() : "";
                          String overview =
                              detail.getOverview() != null ? detail.getOverview() : "";

                          if (title.isBlank() && overview.isBlank()) {
                            log.warn("제목과 설명이 모두 비어있어 색인에서 제외합니다 - spotId: {}", spot.getSpotId());
                            return null;
                          }

                          // ==================================================================
                          // 🚨 모든 null 가능 필드를 안전하게 처리하는 코드입니다.
                          String name = detail.getTitle() != null ? detail.getTitle() : "";
                          String address = detail.getAddress() != null ? detail.getAddress() : "";
                          String category = detail.getType() != null ? detail.getType() : "미분류";
                          // ==================================================================

                          String searchableContent = "관광지 이름: " + title + ", 설명: " + overview;

                          UUID documentId =
                              UUID.nameUUIDFromBytes(
                                  ("spot-" + detail.getSpotId()).getBytes(StandardCharsets.UTF_8));
                          return new Document(
                              documentId.toString(),
                              searchableContent,
                              Map.of(
                                  "type",
                                  "spot",
                                  "contentId",
                                  detail.getSpotId(),
                                  "name",
                                  name, // null-safe 변수 사용
                                  "addr1",
                                  address, // null-safe 변수 사용
                                  "category",
                                  category // null-safe 변수 사용
                                  ));
                        } catch (Exception e) {
                          log.error(
                              "관광지 상세 정보 조회 또는 Document 생성 실패 - contentId: {}",
                              spot.getSpotId(),
                              e);
                          return null;
                        }
                      })
                  .filter(Objects::nonNull)
                  .toList();

          allDocuments.addAll(documentsOnPage);

          if (spotPage.getLast() || spotPage.getContent().isEmpty()) {
            log.info(
                "{} 관광지 {}개 처리 완료", region, (pageNumber * pageSize) + spotPage.getContent().size());
            break;
          }

          pageNumber++;

        } catch (Exception e) {
          log.error("{} 지역의 {} 페이지 색인 중 오류 발생", region, pageNumber, e);
          break;
        }
      }
    }

    if (!allDocuments.isEmpty()) {
      log.info("총 {}개의 관광지 정보를 Qdrant에 저장합니다.", allDocuments.size());
      vectorStore.add(allDocuments);
    } else {
      log.warn("Qdrant에 저장할 관광지 정보가 없습니다.");
    }

    log.info("관광지 정보 총 {}개 색인 완료", allDocuments.size());
  }

  @Transactional
  public void indexFestivalsFromApi() {
    log.info("축제 정보 색인 시작");
    List<String> regions =
        Arrays.asList(
            "경산시", "경주시", "고령군", "구미시", "김천시", "문경시", "봉화군", "상주시", "성주군", "안동시", "영덕군", "영양군",
            "영주시", "영천시", "예천군", "울릉군", "울진군", "의성군", "청도군", "청송군", "칠곡군", "포항시");

    // 모든 지역, 모든 페이지의 축제 정보를 담을 최종 리스트
    List<Document> allFestivalDocuments = new ArrayList<>();

    // 각 지역을 순회
    for (String region : regions) {
      int pageNumber = 0; // 각 지역마다 페이지 번호는 0부터 다시 시작
      int pageSize = 100; // 한 번에 100개씩 데이터를 가져옵니다.

      // 해당 지역의 마지막 페이지까지 무한 반복
      while (true) {
        try {
          // 특정 페이지를 요청하기 위한 Pageable 객체를 생성
          Pageable pageable = PageRequest.of(pageNumber, pageSize);

          // festivalService에 특정 지역의 특정 페이지 데이터를 요청
          // (반환 타입은 PageResponse<FestivalResponse> 여야 합니다.)
          PageResponse<FestivalResponse> festivalPage =
              festivalService.getFestivalsByRegion(region, pageable);

          // 현재 페이지의 축제 목록을 Document로 변환 (기존 로직과 동일)
          List<Document> documentsOnPage =
              festivalPage.getContent().stream()
                  .map(
                      festival -> {
                        try {
                          FestivalDetailResponse detail =
                              festivalService.getFestivalById(festival.getFestivalId());
                          if (detail == null) return null;

                          String title = detail.getTitle() != null ? detail.getTitle() : "";
                          String overview =
                              detail.getOverview() != null ? detail.getOverview() : "";
                          String searchableContent = "축제 이름: " + title + ", 설명: " + overview;

                          UUID documentId =
                              UUID.nameUUIDFromBytes(
                                  ("festival-" + festival.getFestivalId())
                                      .getBytes(StandardCharsets.UTF_8));
                          return new Document(
                              documentId.toString(),
                              searchableContent,
                              Map.of(
                                  "type",
                                  "festival",
                                  "contentId",
                                  festival.getFestivalId(),
                                  "name",
                                  festival.getTitle(),
                                  "addr1",
                                  festival.getAddress()));
                        } catch (Exception e) {
                          log.error("축제 상세 정보 조회 실패 - festivalId: {}", festival.getFestivalId(), e);
                          return null;
                        }
                      })
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());

          // 변환된 Document를 최종 리스트에 추가
          allFestivalDocuments.addAll(documentsOnPage);

          // 현재가 마지막 페이지이거나 내용이 비어있으면, 이 지역의 반복을 종료
          if (festivalPage.getLast() || festivalPage.getContent().isEmpty()) {
            log.info(
                "{} 지역 축제 {}개 처리 완료",
                region,
                (pageNumber * pageSize) + festivalPage.getContent().size());
            break; // 다음 지역으로 넘어갑니다.
          }

          // 다음 페이지를 요청하기 위해 페이지 번호를 1 증가
          pageNumber++;

        } catch (Exception e) {
          log.error("{} 지역의 {} 페이지 색인 중 오류 발생", region, pageNumber, e);
          break; // 오류 발생 시 이 지역의 반복을 중단하고 다음 지역으로 넘어갑니다.
        }
      }
    }

    // 모든 지역에서 수집한 Document들을 VectorStore에 한 번에 저장
    if (!allFestivalDocuments.isEmpty()) {
      vectorStore.add(allFestivalDocuments);
    }
    log.info("축제 정보 총 {}개 색인 완료", allFestivalDocuments.size());
  }
}
