/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.festival.exception;

import org.springframework.http.HttpStatus;

import com.yfive.gbjs.global.error.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FestivalErrorStatus implements BaseErrorCode {
  INVALID_REGION("FESTIVAL001", "유효하지 않은 지역명입니다.", HttpStatus.BAD_REQUEST),
  FESTIVAL_API_ERROR("FESTIVAL002", "축제 API 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
