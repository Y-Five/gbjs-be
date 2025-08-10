/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tts.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "TtsResponse DTO", description = "TTS 변환 응답 반환")
public class TtsResponse {

  @Schema(description = "변환된 음성 URL")
  private String audioUrl;
}
