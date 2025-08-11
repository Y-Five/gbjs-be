/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.spot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "AudioTtsResponse DTO", description = "TTS 파일로 변환할 오디오 가이드 정보 응답 반환")
public class SpotTtsResponse {

  @Schema(description = "가이드 식별자", example = "1")
  private Long guideId;

  @Schema(description = "가이드명", example = "경주 첨성대")
  private String title;

  @Schema(description = "가이드 내용", example = "이 건축물은 바로 첨성대입니다...")
  private String content;

  @Schema(description = "생성된 오디오 식별자", example = "1")
  private Long fileId;
}
