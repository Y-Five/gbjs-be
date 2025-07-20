/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.service;

import com.yfive.gbjs.domain.guide.dto.response.AudioStoryListResponse;
import com.yfive.gbjs.domain.guide.dto.response.GuideListResponse;

/**
 * 가이드 서비스 인터페이스
 *
 * <p>관광지 정보 조회, 오디오 스토리 조회 등의 가이드 관련 기능을 정의합니다.
 *
 * @author YFIVE
 * @since 1.0.0
 */
public interface GuideService {

  /**
   * 관광지 기본 정보 목록 조회
   *
   * <p>관광지 전체 목록을 페이징하여 조회합니다.
   *
   * @param pageNo 페이지 번호
   * @param numOfRows 페이지당 결과 수
   * @return 관광지 목록 응답 객체
   */
  GuideListResponse getThemeBasedList(Integer pageNo, Integer numOfRows);

  /**
   * 관광지 위치기반 정보 목록 조회
   *
   * <p>특정 GPS 좌표를 중심으로 반경 내 관광지 목록을 조회합니다.
   *
   * @param longitude 경도
   * @param latitude 위도
   * @param radius 검색 반경(m)
   * @param pageNo 페이지 번호
   * @param numOfRows 페이지당 결과 수
   * @return 위치기반 관광지 목록 응답 객체
   */
  GuideListResponse getThemeLocationBasedList(
      Double longitude, Double latitude, Integer radius, Integer pageNo, Integer numOfRows);

  /**
   * 관광지 키워드 검색 목록 조회
   *
   * <p>키워드로 관광지를 검색하여 목록을 조회합니다.
   *
   * @param keyword 검색 키워드
   * @param pageNo 페이지 번호
   * @param numOfRows 페이지당 결과 수
   * @return 키워드 검색 관광지 목록 응답 객체
   */
  GuideListResponse getThemeSearchList(String keyword, Integer pageNo, Integer numOfRows);

  /**
   * 오디오 스토리 기본 정보 목록 조회
   *
   * <p>관광지 ID를 기준으로 관련 오디오 스토리 정보를 조회합니다.
   *
   * @param themeId 관광지 ID
   * @param pageNo 페이지 번호
   * @param numOfRows 페이지당 결과 수
   * @return 오디오 스토리 목록 응답 객체
   */
  AudioStoryListResponse getAudioStoryBasedList(String themeId, Integer pageNo, Integer numOfRows);

  /**
   * 오디오 스토리 위치기반 정보 목록 조회
   *
   * <p>좌표를 기준으로 주변 오디오 스토리 목록을 조회합니다.
   *
   * @param longitude 경도
   * @param latitude 위도
   * @param radius 검색 반경(m)
   * @param pageNo 페이지 번호
   * @param numOfRows 페이지당 결과 수
   * @return 위치기반 오디오 스토리 목록 응답 객체
   */
  AudioStoryListResponse getAudioStoryLocationBasedList(
      Double longitude, Double latitude, Integer radius, Integer pageNo, Integer numOfRows);

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
}
