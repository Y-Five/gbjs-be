/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tts.exception;

import org.springframework.http.HttpStatus;

import com.yfive.gbjs.global.error.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TtsErrorStatus implements BaseErrorCode {
  TTS_NOT_FOUND("TTS001", "TTS로 변환할 스크립트가 없습니다.", HttpStatus.NOT_FOUND),
  TTS_API_ERROR("TTS002", "TTS API 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  CREDENTIAL_NOT_FOUND("TTS003", "Google 자격증명 경로가 설정되어 있지 않거나 비어 있습니다.", HttpStatus.BAD_REQUEST),
  CREDENTIAL_ERROR("TTS004", "Google 자격증명 파일을 경로에서 불러오는 데 실패했습니다", HttpStatus.BAD_REQUEST),
  ;

  private final String code;
  private final String message;
  private final HttpStatus status;
}
