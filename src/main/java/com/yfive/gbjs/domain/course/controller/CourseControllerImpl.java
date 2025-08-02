/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

import com.yfive.gbjs.domain.course.dto.request.CourseRequest.CreateCourseRequest;
import com.yfive.gbjs.domain.course.dto.request.CourseRequest.SaveCourseRequest;
import com.yfive.gbjs.domain.course.dto.response.CourseResponse;
import com.yfive.gbjs.domain.course.service.CourseService;
import com.yfive.gbjs.global.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CourseControllerImpl implements CourseController {

  private final CourseService courseService;

  @Override
  public ResponseEntity<ApiResponse<CourseResponse.CourseDetailDTO>> generateCourse(
      CreateCourseRequest request) {
    CourseResponse.CourseDetailDTO response = courseService.generateCourse(request);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Override
  public ResponseEntity<ApiResponse<CourseResponse.CourseDetailDTO>> saveCourse(
      Authentication authentication, SaveCourseRequest request) {
    Long userId = Long.valueOf(authentication.getName());
    CourseResponse.CourseDetailDTO response = courseService.saveCourse(userId, request);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Override
  public ResponseEntity<ApiResponse<CourseResponse.CourseDetailDTO>> getCourse(
      Authentication authentication, Long courseId) {
    Long userId = Long.valueOf(authentication.getName());
    CourseResponse.CourseDetailDTO response = courseService.getCourse(userId, courseId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Override
  public ResponseEntity<ApiResponse<CourseResponse.CourseListDTO>> getMyCourses(
      Authentication authentication) {
    Long userId = Long.valueOf(authentication.getName());
    CourseResponse.CourseListDTO response = courseService.getUserCourses(userId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Override
  public ResponseEntity<ApiResponse<Void>> deleteCourse(
      Authentication authentication, Long courseId) {
    Long userId = Long.valueOf(authentication.getName());
    courseService.deleteCourse(userId, courseId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
