/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.dto.response;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

public class CourseResponse {

  @Builder
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(title = "CourseDetail : 여행 코스 상세 정보")
  public static class CourseDetailDTO {
    @Schema(description = "코스 ID", example = "null")
    private Long id;

    @Schema(description = "여행 제목", example = "경북 3일 여행")
    private String title;

    @Schema(description = "시작 날짜", example = "2025-12-01")
    private LocalDate startDate;

    @Schema(description = "종료 날짜", example = "2025-12-03")
    private LocalDate endDate;

    @Schema(description = "일차별 코스 목록")
    private List<DailyCourseDTO> dailyCourses;
  }

  @Builder
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(title = "DailyCourse : 일차별 코스 정보")
  public static class DailyCourseDTO {
    @Schema(description = "일차", example = "1")
    private Integer dayNumber;

    @Schema(description = "날짜", example = "2025-12-01")
    private LocalDate date;

    @Schema(description = "지역", example = "경주시")
    private String location;

    @Schema(description = "방문 장소 목록")
    private List<SimpleSpotDTO> spots;
  }

  @Builder
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(title = "Spot : 방문 장소 정보")
  public static class SpotDTO {
    @Schema(description = "장소 ID", example = "1")
    private Long spotId;

    @Schema(description = "방문 순서", example = "1")
    private Integer visitOrder;

    @Schema(description = "장소명", example = "불국사")
    private String name;

    @Schema(description = "장소 설명", example = "신라시대의 대표적인 사찰")
    private String description;

    @Schema(description = "주소", example = "경북 경주시 불국로 385")
    private String address;

    @Schema(description = "카테고리", example = "유명 관광지")
    private String category;

    @Schema(description = "이미지 URL")
    private String imageUrl;
  }

  @Builder
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(title = "SimpleSpot : 간략한 방문 장소 정보")
  public static class SimpleSpotDTO {
    @Schema(description = "장소 ID", example = "1")
    private Long spotId;

    @Schema(description = "방문 순서", example = "1")
    private Integer visitOrder;

    @Schema(description = "장소명", example = "불국사")
    private String name;

    @Schema(description = "카테고리", example = "자연환경")
    private String category;

    @Schema(description = "씰 관광지 여부", example = "true")
    private Boolean isSealSpot;

    @Schema(description = "씰 관광지 ID (씰 관광지인 경우)", example = "2")
    private Long sealSpotId;
  }

  @Builder
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(title = "CourseList : 여행 코스 목록")
  public static class CourseListDTO {
    @Schema(description = "코스 목록")
    private List<CourseSummaryDTO> courses;

    @Schema(description = "전체 코스 수", example = "10")
    private int totalCount;
  }

  @Builder
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(title = "CourseSummary : 여행 코스 요약 정보")
  public static class CourseSummaryDTO {
    @Schema(description = "코스 ID", example = "1")
    private Long id;

    @Schema(description = "여행 제목", example = "경북 3일 여행")
    private String title;

    @Schema(description = "시작 날짜", example = "2025-12-01")
    private LocalDate startDate;

    @Schema(description = "종료 날짜", example = "2025-12-03")
    private LocalDate endDate;

    @Schema(description = "총 일수", example = "3")
    private Integer totalDays;

    @Schema(description = "방문 지역", example = "[\"경주시\", \"포항시\", \"안동시\"]")
    private List<String> locations;

    @Schema(description = "코스 내 띠부씰 관광지 ID 목록", example = "[1, 2, 3]")
    private List<Long> sealSpotIds;
  }
}
