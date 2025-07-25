/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.dto.request;

import com.yfive.gbjs.domain.seal.entity.Location;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 코스 생성 요청 DTO
 * 지역과 날짜를 기반으로 띠부씰 여행 코스를 생성하기 위한 요청 데이터
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "코스 생성 요청")
public class CreateCourseRequest {

  @NotNull(message = "지역은 필수입니다.")
  @Schema(description = "여행 지역", example = "GANGNEUNG")
  private Location location;

  @NotNull(message = "시작 날짜는 필수입니다.")
  @Schema(description = "여행 시작 날짜", example = "2025-01-15")
  private LocalDate startDate;

  @NotNull(message = "종료 날짜는 필수입니다.")
  @Schema(description = "여행 종료 날짜", example = "2025-01-17")
  private LocalDate endDate;
}