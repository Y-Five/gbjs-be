/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 코스 저장 요청 DTO
 * 생성된 코스를 저장할 때 제목 정보를 전달하기 위한 데이터
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "코스 저장 요청")
public class SaveCourseRequest {

  @NotBlank(message = "코스 제목은 필수입니다.")
  @Schema(description = "코스 제목", example = "강릉 2박 3일 띠부씰 여행")
  private String title;
}