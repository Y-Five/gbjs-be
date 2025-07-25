/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.dto.response;

import com.yfive.gbjs.domain.course.entity.Course;
import com.yfive.gbjs.domain.seal.entity.Location;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 코스 응답 DTO
 * 여행 코스의 전체 정보와 일별 세부 일정을 전달
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {

  private Long id;
  private String title;
  private Location location;
  private LocalDate startDate;
  private LocalDate endDate;
  private boolean isSaved;
  private List<DailyCourseResponse> dailyCourses;

  /**
   * Course 엔티티를 CourseResponse DTO로 변환
   *
   * @param course 코스 엔티티
   * @return 코스 응답 DTO
   */
  public static CourseResponse of(Course course) {
    return CourseResponse.builder()
        .id(course.getId())
        .title(course.getTitle())
        .location(course.getLocation())
        .startDate(course.getStartDate())
        .endDate(course.getEndDate())
        .isSaved(course.isSaved())
        .dailyCourses(course.getDailyCourses().stream()
            .map(DailyCourseResponse::of)
            .collect(Collectors.toList()))
        .build();
  }
}