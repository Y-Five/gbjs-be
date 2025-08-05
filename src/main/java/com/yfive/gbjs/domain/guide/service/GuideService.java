/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.service;

import com.yfive.gbjs.domain.guide.dto.response.AudioStoryDetailResponse;
import com.yfive.gbjs.domain.guide.dto.response.AudioStoryListResponse;
import com.yfive.gbjs.domain.guide.dto.response.AudioStorySimpleListResponse;

/**
 * 가이드 서비스 인터페이스
 *
 * <p>오디오 스토리 조회 등의 가이드 관련 기능을 정의합니다.
 *
 * @author YFIVE
 * @since 1.0.0
 */
public interface GuideService {

  /**
   * 오디오 스토리 기본 정보 목록 조회
   *
   * <p>관광지 ID를 기준으로 관련 오디오 스토리 정보를 조회합니다.
   *
   * @param spotId 관광지 ID
   * @param tlid 테마 언어 ID
   * @param pageNo 페이지 번호
   * @param numOfRows 페이지당 결과 수
   * @return 오디오 스토리 목록 응답 객체
   */
  AudioStoryListResponse getAudioStoryBasedList(
      String spotId, String tlid, Integer pageNo, Integer numOfRows);

  /**
   * 오디오 스토리 위치기반 정보 목록 조회
   *
   * <p>좌표를 기준으로 오디오 스토리 목록을 조회합니다.
   *
   * @param longitude 경도
   * @param latitude 위도
   * @param pageNo 페이지 번호
   * @param numOfRows 페이지당 결과 수
   * @return 오디오 스토리 목록 응답 객체
   */
  AudioStoryListResponse getAudioStoryLocationBasedList(
      Double longitude, Double latitude, Integer pageNo, Integer numOfRows);

  /**
   * 오디오 스토리 키워드 검색 목록 조회
   *
   * <p>키워드로 오디오 스토리를 검색하여 목록을 조회합니다.
   *
   * @param keyword 검색 키워드
   * @param pageNo 페이지 번호
   * @param numOfRows 페이지당 결과 수
   * @return 키워드 검색 오디오 스토리 목록 응답 객체
   */
  AudioStoryListResponse getAudioStorySearchList(String keyword, Integer pageNo, Integer numOfRows);

  /**
   * 오디오 스토리 간략 목록 조회
   *
   * <p>오디오 스토리의 기본 정보만을 포함한 간략한 목록을 조회합니다.
   *
   * @param spotId 관광지 ID (선택)
   * @param longitude 경도 (위치기반 조회시)
   * @param latitude 위도 (위치기반 조회시)
   * @param keyword 검색 키워드 (키워드 검색시)
   * @param pageNo 페이지 번호
   * @param numOfRows 페이지당 결과 수
   * @return 오디오 스토리 간략 목록 응답 객체
   */
  AudioStorySimpleListResponse getAudioStorySimpleList(
      String spotId,
      Double longitude,
      Double latitude,
      String keyword,
      Integer pageNo,
      Integer numOfRows);

  /**
   * 특정 관광지의 오디오 스토리 상세 정보 조회
   *
   * <p>관광지 ID로 해당 관광지의 모든 오디오 스토리 상세 정보를 조회합니다.
   *
   * @param spotId 관광지 ID
   * @return 오디오 스토리 상세 정보 응답 객체
   */
  AudioStoryDetailResponse getAudioStoryDetail(String spotId);
}
