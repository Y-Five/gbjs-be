/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "PopularSealSpotResponse DTO", description = "인기 띠부씰 관광지 정보 응답")
public class PopularSealSpotResponse {

  @Schema(description = "관광지 ID", example = "1")
  private Long spotId;

  @Schema(description = "관광지명", example = "첨성대")
  private String name;

  @Schema(
      description = "이미지 URL",
      example = "http://tong.visitkorea.or.kr/cms/resource/01/2656601_image2_1.jpg")
  private String imageUrl;

  @Schema(description = "해시태그", example = "[\"야경 명소\", \"필수코스\"]")
  private List<String> hashtag;
}
