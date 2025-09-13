/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.service;

import java.util.List;

import com.yfive.gbjs.domain.course.dto.request.CourseRequest.CreateCourseRequest;
import com.yfive.gbjs.domain.course.dto.request.CourseRequest.SaveCourseRequest;
import com.yfive.gbjs.domain.course.dto.response.CourseResponse;
import com.yfive.gbjs.domain.course.entity.CourseSortBy;
import com.yfive.gbjs.global.error.exception.CustomException;

/** 여행 코스 관련 비즈니스 로직을 처리하는 서비스 인터페이스 */
public interface CourseService {

  /**
   * 여행 코스를 생성합니다. (DB 저장 X)
   *
   * @param request 코스 생성 요청 정보 (시작일, 종료일, 지역 목록)
   * @return 생성된 코스 상세 정보 (일차별 관광지 포함)
   */
  CourseResponse.CourseDetailDTO generateCourse(CreateCourseRequest request);

  /**
   * 생성된 여행 코스를 DB에 저장합니다.
   *
   * @param userId 사용자 ID
   * @param request 코스 저장 요청 정보 (제목, 날짜, 일차별 코스 및 관광지)
   * @return 저장된 코스 상세 정보 (ID 포함)
   */
  CourseResponse.CourseDetailDTO saveCourse(Long userId, SaveCourseRequest request);

  /**
   * 특정 코스의 상세 정보를 조회합니다.
   *
   * @param userId 사용자 ID (권한 확인용)
   * @param courseId 조회할 코스 ID
   * @return 코스 상세 정보
   * @throws CustomException 코스를 찾을 수 없거나 권한이 없는 경우
   */
  CourseResponse.CourseDetailDTO getCourse(Long userId, Long courseId);

  /**
   * 사용자가 저장한 모든 코스 목록을 조회합니다.
   *
   * @param userId 사용자 ID
   * @param locationNames 지역명 리스트
   * @param sortBy 정렬 기준
   * @return 코스 목록 (요약 정보)
   */
  CourseResponse.CourseListDTO getUserCourses(
      Long userId, List<String> locationNames, CourseSortBy sortBy);

  /**
   * 코스를 삭제합니다.
   *
   * @param userId 사용자 ID (권한 확인용)
   * @param courseId 삭제할 코스 ID
   * @throws CustomException 코스를 찾을 수 없거나 권한이 없는 경우
   */
  void deleteCourse(Long userId, Long courseId);
}
