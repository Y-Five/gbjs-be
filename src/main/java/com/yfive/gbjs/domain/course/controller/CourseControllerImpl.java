/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

import com.yfive.gbjs.domain.course.dto.request.CourseRequest.CreateCourseRequest;
import com.yfive.gbjs.domain.course.dto.request.CourseRequest.SaveCourseRequest;
import com.yfive.gbjs.domain.course.dto.response.CourseResponse;
import com.yfive.gbjs.domain.course.entity.CourseSortBy;
import com.yfive.gbjs.domain.course.entity.RecommendationType;
import com.yfive.gbjs.domain.course.service.CourseService;
import com.yfive.gbjs.domain.user.service.UserService;
import com.yfive.gbjs.global.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CourseControllerImpl implements CourseController {

  private final CourseService courseService;
  private final UserService userService;

  @Override
  public ResponseEntity<ApiResponse<CourseResponse.CourseDetailDTO>> generateCourse(
      Authentication authentication, CreateCourseRequest request) {
    CourseResponse.CourseDetailDTO response = courseService.generateCourse(request);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Override
  public ResponseEntity<ApiResponse<CourseResponse.CourseDetailDTO>> saveCourse(
      Authentication authentication, SaveCourseRequest request) {
    Long userId = userService.getCurrentUser().getId();
    CourseResponse.CourseDetailDTO response = courseService.saveCourse(userId, request);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Override
  public ResponseEntity<ApiResponse<CourseResponse.CourseDetailDTO>> getCourse(
      Authentication authentication, Long courseId) {
    Long userId = userService.getCurrentUser().getId();
    CourseResponse.CourseDetailDTO response = courseService.getCourse(userId, courseId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Override
  public ResponseEntity<ApiResponse<CourseResponse.CourseListDTO>> getMyCourses(
      Authentication authentication, List<String> locationNames, CourseSortBy sortBy) {
    Long userId = userService.getCurrentUser().getId();
    CourseResponse.CourseListDTO response =
        courseService.getUserCourses(userId, locationNames, sortBy);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Override
  public ResponseEntity<ApiResponse<Void>> deleteCourse(
      Authentication authentication, Long courseId) {
    Long userId = userService.getCurrentUser().getId();
    courseService.deleteCourse(userId, courseId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Override
  public ResponseEntity<ApiResponse<List<CourseResponse.RecommendedCourseDTO>>>
      getRecommendedCourses(RecommendationType type) {
    List<CourseResponse.RecommendedCourseDTO> response = courseService.getRecommendedCourses(type);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
