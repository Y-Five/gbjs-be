/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.service;

import com.yfive.gbjs.domain.seal.dto.response.SealProductResponse;
import com.yfive.gbjs.domain.seal.dto.response.SealResponse;
import com.yfive.gbjs.domain.seal.dto.response.UserSealResponse;
import com.yfive.gbjs.domain.seal.entity.SortBy;

/** 띠부씰 서비스 인터페이스 띠부씰 관련 비즈니스 로직을 정의 */
public interface SealService {

  /**
   * 등록된 모든 띠부씰 조회
   *
   * @param sortBy 정렬 옵션
   * @return 전체 띠부씰 목록
   */
  SealResponse.SealListDTO getAllSeals(SortBy sortBy);

  /**
   * 특정 사용자의 띠부씰 수집 현황 조회
   *
   * @param sortBy 정렬 옵션
   * @return 사용자의 띠부씰 수집 현황 (수집한 것/수집하지 않은 것 포함)
   */
  UserSealResponse.UserSealListDTO getUserSeals(SortBy sortBy);

  /**
   * 띠부씰 상품 목록 조회
   *
   * @return 띠부씰 상품 목록
   */
  SealProductResponse.SealProductListDTO getSealProducts();

  /**
   * 현재 위치 기반 가까운 띠부씰 조회
   *
   * @param latitude 현재 위치 위도
   * @param longitude 현재 위치 경도
   * @return 가까운 띠부씰 4개 목록
   */
  SealResponse.NearbySealListDTO getNearbySeals(Double latitude, Double longitude);

  /**
   * 위치 인증을 통한 띠부씰 획득
   *
   * @param sealId 획득할 띠부씰 ID
   * @param latitude 현재 위치 위도
   * @param longitude 현재 위치 경도
   * @return 띠부씰 획득 결과
   */
  SealResponse.CollectSealResultDTO collectSeal(Long sealId, Double latitude, Double longitude);

  /**
   * 띠부씰 획득 실패 메시지 조회
   *
   * @param sealId 띠부씰 ID
   * @return 지역에 따른 실패 메시지
   */
  String getFailureMessage(Long sealId);

  /**
   * 획득한 띠부씰 삭제
   *
   * @param sealId 삭제할 띠부씰 ID
   */
  void deleteCollectedSeal(Long sealId);
}
