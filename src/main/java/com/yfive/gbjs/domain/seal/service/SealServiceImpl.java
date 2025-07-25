/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.service;

import com.yfive.gbjs.domain.seal.dto.response.SealListResponse;
import com.yfive.gbjs.domain.seal.dto.response.SealProductListResponse;
import com.yfive.gbjs.domain.seal.dto.response.SealProductResponse;
import com.yfive.gbjs.domain.seal.dto.response.SealResponse;
import com.yfive.gbjs.domain.seal.dto.response.UserSealListResponse;
import com.yfive.gbjs.domain.seal.dto.response.UserSealResponse;
import com.yfive.gbjs.domain.seal.entity.Seal;
import com.yfive.gbjs.domain.seal.entity.SealProduct;
import com.yfive.gbjs.domain.seal.entity.mapper.UserSeal;
import com.yfive.gbjs.domain.seal.repository.SealProductRepository;
import com.yfive.gbjs.domain.seal.repository.SealRepository;
import com.yfive.gbjs.domain.seal.repository.UserSealRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 띠부씰 서비스 구현체
 * 띠부씰 관련 비즈니스 로직을 구현
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SealServiceImpl implements SealService {

  private final SealRepository sealRepository;
  private final UserSealRepository userSealRepository;
  private final SealProductRepository sealProductRepository;

  /**
   * 등록된 모든 띠부씰을 조회하여 반환
   */
  @Override
  public SealListResponse getAllSeals() {
    List<Seal> seals = sealRepository.findAll();
    List<SealResponse> sealResponses = seals.stream()
        .map(SealResponse::of)
        .collect(Collectors.toList());

    return SealListResponse.of(sealResponses);
  }

  /**
   * 특정 사용자의 띠부씰 수집 현황을 조회
   * 모든 띠부씰에 대해 사용자의 수집 여부와 수집 시간을 포함하여 반환
   */
  @Override
  public UserSealListResponse getUserSeals(Long userId) {
    // 모든 띠부씰 조회
    List<Seal> allSeals = sealRepository.findAll();
    // 사용자가 수집한 띠부씰 조회
    List<UserSeal> userSeals = userSealRepository.findByUserId(userId);

    // 사용자가 수집한 띠부씰을 Map으로 변환 (빠른 조회를 위해)
    Map<Long, LocalDateTime> userSealMap = userSeals.stream()
        .collect(Collectors.toMap(
            us -> us.getSeal().getId(),
            UserSeal::getCollectedAt
        ));

    // 모든 띠부씰에 대해 사용자의 수집 정보를 합쳐서 반환
    List<UserSealResponse> userSealResponses = allSeals.stream()
        .map(seal -> {
          boolean collected = userSealMap.containsKey(seal.getId());
          LocalDateTime collectedAt = userSealMap.get(seal.getId());
          return UserSealResponse.of(seal, collected, collectedAt);
        })
        .collect(Collectors.toList());

    return UserSealListResponse.of(userSealResponses);
  }

  /**
   * 등록된 모든 띠부씰 상품을 조회하여 반환
   */
  @Override
  public SealProductListResponse getSealProducts() {
    List<SealProduct> products = sealProductRepository.findAll();
    List<SealProductResponse> productResponses = products.stream()
        .map(SealProductResponse::of)
        .collect(Collectors.toList());

    return SealProductListResponse.of(productResponses);
  }
}
