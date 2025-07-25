/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 코스 목록 응답 DTO
 * 여러 개의 코스 정보를 목록 형태로 전달
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseListResponse {

  private List<CourseResponse> courses;
  private int totalCount;

  /**
   * CourseResponse 목록을 CourseListResponse DTO로 변환
   *
   * @param courses 코스 응답 DTO 목록
   * @return 코스 목록 응답 DTO
   */
  public static CourseListResponse of(List<CourseResponse> courses) {
    return CourseListResponse.builder()
        .courses(courses)
        .totalCount(courses.size())
        .build();
  }
}