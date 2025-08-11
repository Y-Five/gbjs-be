/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.festival.dto.response;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "FestivalResponse DTO", description = "축제 응답 리스트 반환")
public class FestivalResponse {

  @JsonProperty("contentid")
  @Schema(description = "축제 식별자", example = "2867141")
  private String festivalId;

  @JsonProperty("firstimage")
  @Schema(
      description = "축제 포스터 URL",
      example = "http://tong.visitkorea.or.kr/cms/resource/61/3516661_image2_1.jpg")
  private String posterUrl;

  @JsonProperty("addr1")
  @Schema(description = "축제 위치", example = "경상북도 안동시 영가로 16 (동부동)")
  private String address;

  @Schema(description = "축제명", example = "경북 K-스토리 페스티벌")
  private String title;

  @JsonProperty("eventstartdate")
  @JsonFormat(pattern = "yyyyMMdd")
  @Schema(description = "축제 시작일", example = "20250919")
  private LocalDate startDate;

  @JsonProperty("eventenddate")
  @JsonFormat(pattern = "yyyyMMdd")
  @Schema(description = "축제 종료일", example = "20250920")
  private LocalDate endDate;
}
