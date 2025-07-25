/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.dto.response;

import com.yfive.gbjs.domain.course.entity.DailyCourse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 일별 코스 응답 DTO
 * 각 날짜별 여행 일정과 방문 장소 정보를 전달
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyCourseResponse {

  private Long id;
  private Integer dayNumber;
  private List<CourseSpotResponse> spots;

  /**
   * DailyCourse 엔티티를 DailyCourseResponse DTO로 변환
   *
   * @param dailyCourse 일별 코스 엔티티
   * @return 일별 코스 응답 DTO
   */
  public static DailyCourseResponse of(DailyCourse dailyCourse) {
    return DailyCourseResponse.builder()
        .id(dailyCourse.getId())
        .dayNumber(dailyCourse.getDayNumber())
        .spots(dailyCourse.getCourseSpots().stream()
            .map(CourseSpotResponse::of)
            .collect(Collectors.toList()))
        .build();
  }
}