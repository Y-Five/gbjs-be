/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.controller;

import com.yfive.gbjs.domain.course.dto.request.CreateCourseRequest;
import com.yfive.gbjs.domain.course.dto.request.SaveCourseRequest;
import com.yfive.gbjs.domain.course.dto.response.CourseListResponse;
import com.yfive.gbjs.domain.course.dto.response.CourseResponse;
import com.yfive.gbjs.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 코스 컨트롤러 인터페이스
 * 띠부씰 여행 코스 관련 API 엔드포인트를 정의
 */
@Tag(name = "코스", description = "띠부씰 여행 코스 관련 API")
@RequestMapping("/api/courses")
public interface CourseController {

  @PostMapping
  @Operation(summary = "코스 생성", description = "선택한 지역과 날짜를 기반으로 띠부씰 여행 코스를 생성합니다.")
  ResponseEntity<ApiResponse<CourseResponse>> createCourse(
      @Parameter(description = "회원 ID", required = true)
      @RequestParam Long userId,
      @Valid @RequestBody CreateCourseRequest request
  );

  @PutMapping("/{courseId}/save")
  @Operation(summary = "코스 저장", description = "생성된 코스를 저장합니다.")
  ResponseEntity<ApiResponse<CourseResponse>> saveCourse(
      @Parameter(description = "회원 ID", required = true)
      @RequestParam Long userId,
      @Parameter(description = "코스 ID", required = true)
      @PathVariable Long courseId,
      @Valid @RequestBody SaveCourseRequest request
  );

  @GetMapping("/saved")
  @Operation(summary = "저장된 코스 조회", description = "회원이 저장한 코스 목록을 조회합니다.")
  ResponseEntity<ApiResponse<CourseListResponse>> getSavedCourses(
      @Parameter(description = "회원 ID", required = true)
      @RequestParam Long userId
  );
}