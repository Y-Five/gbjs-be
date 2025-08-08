/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yfive.gbjs.domain.seal.converter.SealConverter;
import com.yfive.gbjs.domain.seal.converter.SealProductConverter;
import com.yfive.gbjs.domain.seal.converter.UserSealConverter;
import com.yfive.gbjs.domain.seal.dto.response.SealProductResponse;
import com.yfive.gbjs.domain.seal.dto.response.SealResponse;
import com.yfive.gbjs.domain.seal.dto.response.UserSealResponse;
import com.yfive.gbjs.domain.seal.entity.Rarity;
import com.yfive.gbjs.domain.seal.entity.Seal;
import com.yfive.gbjs.domain.seal.entity.SortBy;
import com.yfive.gbjs.domain.seal.entity.mapper.UserSeal;
import com.yfive.gbjs.domain.seal.repository.SealProductRepository;
import com.yfive.gbjs.domain.seal.repository.SealRepository;
import com.yfive.gbjs.domain.seal.repository.UserSealRepository;
import com.yfive.gbjs.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

/** 띠부씰 서비스 구현체 띠부씰 관련 비즈니스 로직을 구현 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SealServiceImpl implements SealService {

  private final SealRepository sealRepository;
  private final UserSealRepository userSealRepository;
  private final SealProductRepository sealProductRepository;
  private final SealProductConverter sealProductConverter;
  private final SealConverter sealConverter;
  private final UserSealConverter userSealConverter;
  private final UserService userService;

  /** 등록된 모든 띠부씰을 조회하여 반환 */
  @Override
  public SealResponse.SealListDTO getAllSeals(SortBy sortBy) {
    List<Seal> seals = sealRepository.findAll();

    // 정렬 적용
    List<SealResponse.SealDTO> sealDTOs =
        seals.stream()
            .map(sealConverter::toDTO)
            .sorted(getSealComparator(sortBy))
            .collect(Collectors.toList());

    return sealConverter.toListDTO(sealDTOs);
  }

  /** 특정 사용자의 띠부씰 수집 현황을 조회 모든 띠부씰에 대해 사용자의 수집 여부와 수집 시간을 포함하여 반환 */
  @Override
  public UserSealResponse.UserSealListDTO getUserSeals(SortBy sortBy) {
    Long userId = userService.getCurrentUser().getId();
    List<Seal> allSeals = sealRepository.findAll();
    List<UserSeal> userSeals = userSealRepository.findByUserId(userId);

    // 사용자가 수집한 띠부씰을 Map으로 변환 (빠른 조회를 위해)
    Map<Long, UserSeal> userSealMap =
        userSeals.stream().collect(Collectors.toMap(us -> us.getSeal().getId(), us -> us));

    // 모든 띠부씰에 대해 사용자의 수집 정보를 합쳐서 반환
    List<UserSealResponse.UserSealDTO> userSealDTOs =
        allSeals.stream()
            .map(
                seal -> {
                  UserSeal userSeal = userSealMap.get(seal.getId());
                  boolean collected = userSeal != null && userSeal.getCollected();
                  LocalDateTime collectedAt = userSeal != null ? userSeal.getCollectedAt() : null;
                  return userSealConverter.toDTO(seal, collected, collectedAt);
                })
            .sorted(getUserSealComparator(sortBy))
            .collect(Collectors.toList());

    return userSealConverter.toListDTO(userSealDTOs);
  }

  /** 등록된 모든 띠부씰 상품을 조회하여 반환 */
  @Override
  public SealProductResponse.SealProductListDTO getSealProducts() {
    List<com.yfive.gbjs.domain.seal.entity.SealProduct> products = sealProductRepository.findAll();
    List<SealProductResponse.SealProductDTO> productDTOs =
        products.stream().map(sealProductConverter::toDTO).collect(Collectors.toList());

    return sealProductConverter.toListDTO(productDTOs);
  }

  /** Seal 정렬을 위한 Comparator 생성 */
  private java.util.Comparator<SealResponse.SealDTO> getSealComparator(SortBy sortBy) {
    switch (sortBy) {
      case RARITY:
        // 희귀한 순서대로 정렬 (RED > GREEN > BLUE), 같은 희귀도면 번호순
        return java.util.Comparator.comparing(
                (SealResponse.SealDTO s) -> getRarityOrder(s.getRarity()))
            .thenComparing(SealResponse.SealDTO::getNumber);
      case LOCATION:
        // 지역명대로 정렬, 같은 지역명이면 번호순
        return java.util.Comparator.comparing(SealResponse.SealDTO::getLocationName)
            .thenComparing(SealResponse.SealDTO::getNumber);
      case NUMBER:
      default:
        // 번호순: 1번부터 정렬 (기본값)
        return java.util.Comparator.comparing(SealResponse.SealDTO::getNumber);
    }
  }

  /** UserSeal 정렬을 위한 Comparator 생성 */
  private java.util.Comparator<UserSealResponse.UserSealDTO> getUserSealComparator(SortBy sortBy) {
    switch (sortBy) {
      case RARITY:
        // 희귀한 순서대로 정렬 (RED > GREEN > BLUE), 같은 희귀도면 번호순
        return java.util.Comparator.comparing(
                (UserSealResponse.UserSealDTO s) -> getRarityOrder(s.getRarity()))
            .thenComparing(UserSealResponse.UserSealDTO::getNumber);
      case COLLECTED:
        // 먼저 수집한 상품부터 보여주기 (수집한 것 우선, 수집 시간 오름차순)
        return java.util.Comparator.comparing(UserSealResponse.UserSealDTO::isCollected)
            .reversed() // 수집한 것 먼저
            .thenComparing(
                (UserSealResponse.UserSealDTO s) -> {
                  // 수집되지 않은 경우 MAX 값으로 처리하여 뒤로 보냄
                  if (s.getCollectedAt() == null) {
                    return java.time.LocalDateTime.MAX;
                  }
                  return s.getCollectedAt();
                }) // 수집 시간 오름차순
            .thenComparing(UserSealResponse.UserSealDTO::getNumber); // 같은 시간이면 번호순
      case LOCATION:
        // 지역명대로 정렬, 같은 지역명이면 번호순
        return java.util.Comparator.comparing(UserSealResponse.UserSealDTO::getLocationName)
            .thenComparing(UserSealResponse.UserSealDTO::getNumber);
      case NUMBER:
      default:
        // 번호순: 1번부터 정렬 (기본값)
        return java.util.Comparator.comparing(UserSealResponse.UserSealDTO::getNumber);
    }
  }

  /** 희귀도 정렬 순서 정의 (RED(희귀) > GREEN(보통) > BLUE(흔함)) */
  private int getRarityOrder(Rarity rarity) {
    if (rarity == Rarity.RED) {
      return 1; // 희귀 (빨간색)
    } else if (rarity == Rarity.GREEN) {
      return 2; // 보통 (초록색)
    } else if (rarity == Rarity.BLUE) {
      return 3; // 흔함 (파란색)
    } else {
      return 4;
    }
  }
}
