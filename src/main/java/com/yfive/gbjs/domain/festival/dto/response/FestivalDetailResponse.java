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
import lombok.Setter;

@Getter
@Builder
@Schema(title = "FestivalResponse DTO", description = "축제 정보 응답 반환")
public class FestivalDetailResponse {

  @JsonProperty("firstimage")
  @Schema(
      description = "축제 포스터 URL",
      example = "http://tong.visitkorea.or.kr/cms/resource/61/3516661_image2_1.jpg")
  private String posterUrl;

  @Schema(description = "축제명", example = "경북 K-스토리 페스티벌")
  private String title;

  @JsonProperty("eventstartdate")
  @JsonFormat(pattern = "yyyyMMdd")
  @Setter
  @Schema(description = "축제 시작일", example = "20250919")
  private LocalDate startDate;

  @JsonProperty("eventenddate")
  @JsonFormat(pattern = "yyyyMMdd")
  @Setter
  @Schema(description = "축제 종료일", example = "20250920")
  private LocalDate endDate;

  @Schema(description = "축제 설명", example = "2025 경북 K-스토리 페스티벌은 ‘K-스토리, 경북에 펼치다’를 주제로 열리는...")
  private String overview;

  @JsonProperty("addr1")
  @Schema(description = "축제 위치", example = "경상북도 안동시 영가로 16 (동부동)")
  private String address;

  @Schema(description = "축제 연락처", example = "054-840-7044")
  private String tel;

  @JsonProperty("homepage")
  @Setter
  @Schema(description = "축제 홈페이지 URL", example = "https://www.storyg.or.kr/home/sub8/sub1.asp")
  private String homepageUrl;
}
