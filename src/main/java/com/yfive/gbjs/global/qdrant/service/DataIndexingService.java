/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.qdrant.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yfive.gbjs.domain.seal.entity.Seal;
import com.yfive.gbjs.domain.seal.entity.SealProduct;
import com.yfive.gbjs.domain.seal.entity.SealSpot;
import com.yfive.gbjs.domain.seal.repository.SealProductRepository;
import com.yfive.gbjs.domain.seal.repository.SealRepository;
import com.yfive.gbjs.domain.seal.repository.SealSpotRepository;
import com.yfive.gbjs.domain.user.entity.User;
import com.yfive.gbjs.domain.user.repository.UserRepository;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.CreateCollection;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DataIndexingService {

  private final VectorStore vectorStore;
  private final SealRepository sealRepository;
  private final SealSpotRepository sealSpotRepository;
  private final SealProductRepository sealProductRepository;
  private final UserRepository userRepository;
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
                          + ", 관광지 ID: "
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
                          + ", 설명: "
                          + spot.getDescription()
                          + ", 위치: "
                          + spot.getLocation().name()
                          + ", 주소: "
                          + spot.getAddress()
                          + ", 카테고리: "
                          + (spot.getCategory() != null ? spot.getCategory().name() : "없음")
                          + ", 오디오 가이드 ID: "
                          + (spot.getAudioGuide() != null ? spot.getAudioGuide().getId() : "없음");

                  UUID documentId =
                      UUID.nameUUIDFromBytes(
                          ("seal_spot-" + spot.getId()).getBytes(StandardCharsets.UTF_8));
                  return new Document(
                      documentId.toString(),
                      searchableContent,
                      Map.of("entity_type", "seal_spot", "sealSpotId", spot.getId()));
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
}
