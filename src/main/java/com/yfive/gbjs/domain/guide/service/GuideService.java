/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.service;

import java.util.List;

import com.yfive.gbjs.domain.guide.dto.response.AudioDetailResponse;
import com.yfive.gbjs.domain.guide.dto.response.CoordinateValidationResponse;

/**
 * 가이드 서비스 인터페이스
 *
 * <p>오디오 데이터 동기화 기능을 정의합니다.
 *
 * @author YFIVE
 * @since 1.0.0
 */
public interface GuideService {

  /**
   * 경상북도 지역의 모든 오디오 스토리를 외부 API에서 가져와 DB에 저장합니다.
   *
   * @return 동기화된 데이터 개수
   */
  int syncGyeongbukAudioStories();

  /**
   * 관광지명으로 오디오 가이드를 정확히 조회합니다.
   *
   * @param title 관광지명
   * @return 일치하는 오디오 가이드 목록
   */
  List<AudioDetailResponse> searchAudioGuideByTitle(String title);

  /**
   * 키워드로 오디오 가이드를 검색합니다. (부분 일치)
   *
   * @param title 검색 키워드
   * @return 키워드가 포함된 오디오 가이드 목록
   */
  List<AudioDetailResponse> searchAudioGuideByTitleLike(String title);

  /**
   * DB에 저장된 모든 오디오 가이드 데이터의 좌표를 검증합니다.
   *
   * @return 좌표 검증 결과
   */
  CoordinateValidationResponse validateStoredCoordinates();

  /**
   * 특정 좌표가 경북 지역에 속하는지 테스트합니다.
   *
   * @param latitude 위도
   * @param longitude 경도
   * @return 경북 지역 포함 여부
   */
  boolean testCoordinate(double latitude, double longitude);

  /**
   * 경북 지역 외부에 있는 모든 오디오 가이드 데이터를 삭제합니다.
   *
   * @return 삭제된 데이터 개수
   */
  int deleteOutsideGyeongbukData();
}
