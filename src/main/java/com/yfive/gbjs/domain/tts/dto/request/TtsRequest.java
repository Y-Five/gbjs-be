/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tts.dto.request;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "TtsRequest DTO", description = "TTS 변환을 위한 스크립트 데이터 전송")
public class TtsRequest {

  @NotBlank(message = "스크립트 항목은 필수입니다.")
  @Schema(description = "음성 변환할 스크립트")
  private String script;
}
