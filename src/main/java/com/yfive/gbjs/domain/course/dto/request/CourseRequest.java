/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.dto.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.*;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

public class CourseRequest {

  @Builder
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(title = "CreateCourseRequest : 여행 코스 생성 요청")
  public static class CreateCourseRequest {

    @NotNull
    @Schema(description = "여행 시작 날짜", example = "2025-12-01")
    private LocalDate startDate;

    @NotNull
    @Schema(description = "여행 종료 날짜", example = "2025-12-03")
    private LocalDate endDate;

    @NotNull
    @Size(min = 1)
    @Schema(description = "방문할 지역 목록", example = "[\"경주시\", \"포항시\", \"안동시\"]")
    private List<String> locations;
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Schema(title = "SaveCourseRequest : 생성된 코스 저장 요청 (생성 API 응답과 동일 형식)")
  public static class SaveCourseRequest {

    @Schema(description = "여행 제목", example = "경주, 포항 3일 여행")
    private String title;

    @NotNull(message = "시작 날짜는 필수입니다.")
    @FutureOrPresent(message = "시작 날짜는 현재 또는 미래여야 합니다.")
    @Schema(description = "여행 시작 날짜", example = "2025-12-01")
    private LocalDate startDate;

    @NotNull(message = "종료 날짜는 필수입니다.")
    @Schema(description = "여행 종료 날짜", example = "2025-12-03")
    private LocalDate endDate;

    @NotEmpty(message = "일차별 코스 정보는 필수입니다.")
    @Schema(description = "일차별 코스 정보")
    private List<DailyCourseRequest> dailyCourses;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(title = "DailyCourseRequest : 일차별 코스 정보")
    public static class DailyCourseRequest {
      @NotNull(message = "일차는 필수입니다.")
      @Schema(description = "일차", example = "1")
      private Integer dayNumber;

      @NotNull(message = "날짜는 필수입니다.")
      @Schema(description = "날짜", example = "2025-12-01")
      private LocalDate date;

      @NotNull(message = "지역은 필수입니다.")
      @Schema(description = "지역", example = "경주시")
      private String location;

      @Schema(description = "방문 장소 목록")
      private List<SpotRequest> spots;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(title = "SpotRequest : 방문 장소 정보")
    public static class SpotRequest {
      @NotNull(message = "장소 ID는 필수입니다.")
      @Schema(description = "장소 ID", example = "5")
      private Long spotId;

      @NotNull(message = "방문 순서는 필수입니다.")
      @Min(value = 1, message = "방문 순서는 1 이상이어야 합니다.")
      @Schema(description = "방문 순서", example = "1")
      private Integer visitOrder;

      @Schema(description = "장소명", example = "대릉원")
      private String name;

      @Schema(description = "카테고리", example = "유명 관광지")
      private String category;
    }
  }
}
