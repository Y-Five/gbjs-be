/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.chat.dto.request;

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
@Schema(title = "ChatRequest DTO", description = "챗봇 질문을 위한 데이터 전송")
public class ChatRequest {

  @NotBlank(message = "질문 항목은 필수입니다.")
  @Schema(description = "사용자 질문", example = "씰은 어떻게 모으나요?")
  private String question;
}
