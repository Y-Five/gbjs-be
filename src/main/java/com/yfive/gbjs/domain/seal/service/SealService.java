/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.service;

import com.yfive.gbjs.domain.seal.dto.response.SealProductResponse;
import com.yfive.gbjs.domain.seal.dto.response.SealResponse;
import com.yfive.gbjs.domain.seal.dto.response.UserSealResponse;

/** 띠부씰 서비스 인터페이스 띠부씰 관련 비즈니스 로직을 정의 */
public interface SealService {

  /**
   * 등록된 모든 띠부씰 조회
   *
   * @return 전체 띠부씰 목록
   */
  SealResponse.SealListDTO getAllSeals();

  /**
   * 특정 사용자의 띠부씰 수집 현황 조회
   *
   * @return 사용자의 띠부씰 수집 현황 (수집한 것/수집하지 않은 것 포함)
   */
  UserSealResponse.UserSealListDTO getUserSeals();

  /**
   * 띠부씰 상품 목록 조회
   *
   * @return 띠부씰 상품 목록
   */
  SealProductResponse.SealProductListDTO getSealProducts();
}
