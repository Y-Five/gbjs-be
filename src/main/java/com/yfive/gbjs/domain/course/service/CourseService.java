/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.service;

import com.yfive.gbjs.domain.course.dto.request.CreateCourseRequest;
import com.yfive.gbjs.domain.course.dto.request.SaveCourseRequest;
import com.yfive.gbjs.domain.course.dto.response.CourseListResponse;
import com.yfive.gbjs.domain.course.dto.response.CourseResponse;

/**
 * 코스 서비스 인터페이스
 * 띠부씰 여행 코스 관련 비즈니스 로직을 정의
 */
public interface CourseService {

  /**
   * 지역과 날짜를 기반으로 여행 코스 생성
   *
   * @param userId 사용자 ID
   * @param request 코스 생성 요청 정보
   * @return 생성된 코스 정보
   */
  CourseResponse createCourse(Long userId, CreateCourseRequest request);

  /**
   * 생성된 코스를 저장
   *
   * @param userId 사용자 ID
   * @param courseId 코스 ID
   * @param request 코스 저장 요청 정보 (제목 포함)
   * @return 저장된 코스 정보
   */
  CourseResponse saveCourse(Long userId, Long courseId, SaveCourseRequest request);

  /**
   * 사용자가 저장한 코스 목록 조회
   *
   * @param userId 사용자 ID
   * @return 저장된 코스 목록
   */
  CourseListResponse getSavedCourses(Long userId);
}