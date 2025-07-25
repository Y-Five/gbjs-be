/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.controller;

import com.yfive.gbjs.domain.course.dto.request.CreateCourseRequest;
import com.yfive.gbjs.domain.course.dto.request.SaveCourseRequest;
import com.yfive.gbjs.domain.course.dto.response.CourseListResponse;
import com.yfive.gbjs.domain.course.dto.response.CourseResponse;
import com.yfive.gbjs.domain.course.service.CourseService;
import com.yfive.gbjs.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * 코스 컨트롤러 구현체
 * 띠부씰 여행 코스 관련 HTTP 요청을 처리하고 적절한 서비스로 위임
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class CourseControllerImpl implements CourseController {

  private final CourseService courseService;

  @Override
  public ResponseEntity<ApiResponse<CourseResponse>> createCourse(Long userId, CreateCourseRequest request) {
    log.info("Create course request for userId: {}, location: {}, dates: {} ~ {}", 
        userId, request.getLocation(), request.getStartDate(), request.getEndDate());
    CourseResponse response = courseService.createCourse(userId, request);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Override
  public ResponseEntity<ApiResponse<CourseResponse>> saveCourse(Long userId, Long courseId, SaveCourseRequest request) {
    log.info("Save course request for userId: {}, courseId: {}", userId, courseId);
    CourseResponse response = courseService.saveCourse(userId, courseId, request);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Override
  public ResponseEntity<ApiResponse<CourseListResponse>> getSavedCourses(Long userId) {
    log.info("Get saved courses request for userId: {}", userId);
    CourseListResponse response = courseService.getSavedCourses(userId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}