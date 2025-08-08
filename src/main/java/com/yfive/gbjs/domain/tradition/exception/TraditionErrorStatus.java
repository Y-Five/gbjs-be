/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tradition.exception;

import org.springframework.http.HttpStatus;

import com.yfive.gbjs.global.error.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TraditionErrorStatus implements BaseErrorCode {
  TRADITION_API_ERROR(
      "TRADITION001", "전통문화 API 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  TRADITION_NOT_FOUND("TRADITION002", "전통문화를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  ;

  private final String code;
  private final String message;
  private final HttpStatus status;
}
