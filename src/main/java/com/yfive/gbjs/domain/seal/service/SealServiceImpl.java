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
import com.yfive.gbjs.domain.seal.entity.Seal;
import com.yfive.gbjs.domain.seal.entity.mapper.UserSeal;
import com.yfive.gbjs.domain.seal.repository.SealProductRepository;
import com.yfive.gbjs.domain.seal.repository.SealRepository;
import com.yfive.gbjs.domain.seal.repository.UserSealRepository;

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

  /** 등록된 모든 띠부씰을 조회하여 반환 */
  @Override
  public SealResponse.SealListDTO getAllSeals() {
    List<Seal> seals = sealRepository.findAll();
    List<SealResponse.SealDTO> sealDTOs =
        seals.stream().map(sealConverter::toDTO).collect(Collectors.toList());

    return sealConverter.toListDTO(sealDTOs);
  }

  /** 특정 사용자의 띠부씰 수집 현황을 조회 모든 띠부씰에 대해 사용자의 수집 여부와 수집 시간을 포함하여 반환 */
  @Override
  public UserSealResponse.UserSealListDTO getUserSeals(Long userId) {
    List<Seal> allSeals = sealRepository.findAll();
    List<UserSeal> userSeals = userSealRepository.findByUserId(userId);

    // 사용자가 수집한 띠부씰을 Map으로 변환 (빠른 조회를 위해)
    Map<Long, LocalDateTime> userSealMap =
        userSeals.stream()
            .collect(Collectors.toMap(us -> us.getSeal().getId(), UserSeal::getCollectedAt));

    // 모든 띠부씰에 대해 사용자의 수집 정보를 합쳐서 반환
    List<UserSealResponse.UserSealDTO> userSealDTOs =
        allSeals.stream()
            .map(
                seal -> {
                  boolean collected = userSealMap.containsKey(seal.getId());
                  LocalDateTime collectedAt = userSealMap.get(seal.getId());
                  return userSealConverter.toDTO(seal, collected, collectedAt);
                })
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
}
