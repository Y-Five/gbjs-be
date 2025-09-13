/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.yfive.gbjs.domain.course.dto.request.CourseRequest.CreateCourseRequest;
import com.yfive.gbjs.domain.course.dto.request.CourseRequest.SaveCourseRequest;
import com.yfive.gbjs.domain.course.dto.response.CourseResponse;
import com.yfive.gbjs.domain.course.entity.CourseSortBy;
import com.yfive.gbjs.global.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "코스", description = "여행 코스 관련 API")
@RequestMapping("/api/courses")
public interface CourseController {

  @Operation(summary = "여행 코스 생성", description = "여행 날짜와 지역을 입력받아 코스를 생성합니다. (저장 X)")
  @PostMapping("/generate")
  ResponseEntity<ApiResponse<CourseResponse.CourseDetailDTO>> generateCourse(
      @Parameter(hidden = true) Authentication authentication,
      @Valid @RequestBody CreateCourseRequest request);

  @Operation(summary = "여행 코스 저장", description = "생성된 코스를 저장합니다.")
  @PostMapping
  ResponseEntity<ApiResponse<CourseResponse.CourseDetailDTO>> saveCourse(
      @Parameter(hidden = true) Authentication authentication,
      @Valid @RequestBody SaveCourseRequest request);

  @Operation(summary = "내 여행 코스 목록 조회", description = "사용자가 저장한 코스 목록을 조회합니다.")
  @GetMapping("/users")
  ResponseEntity<ApiResponse<CourseResponse.CourseListDTO>> getMyCourses(
      @Parameter(hidden = true) Authentication authentication,
      @RequestParam(required = false)
          @Parameter(description = "지역 이름 (여러 개 가능)", example = "안동시,경주시")
          List<String> locationNames,
      @RequestParam(defaultValue = "LATEST")
          @Parameter(
              description = "정렬 옵션 (LATEST: 코스 저장 최신순, OLDEST: 코스 저장 오래된순)",
              example = "LATEST")
          CourseSortBy sortBy);

  @Operation(summary = "여행 코스 상세 조회", description = "코스 ID로 상세 정보를 조회합니다.")
  @GetMapping("/{courseId}")
  ResponseEntity<ApiResponse<CourseResponse.CourseDetailDTO>> getCourse(
      @Parameter(hidden = true) Authentication authentication,
      @PathVariable @Parameter(description = "코스 ID", example = "1") Long courseId);

  @Operation(summary = "여행 코스 삭제", description = "코스 ID로 코스를 삭제합니다.")
  @DeleteMapping("/{courseId}")
  ResponseEntity<ApiResponse<Void>> deleteCourse(
      @Parameter(hidden = true) Authentication authentication,
      @PathVariable @Parameter(description = "삭제할 코스 ID", example = "1") Long courseId);
}
