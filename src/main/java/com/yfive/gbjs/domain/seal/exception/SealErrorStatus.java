/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.exception;

import org.springframework.http.HttpStatus;

import com.yfive.gbjs.global.error.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SealErrorStatus implements BaseErrorCode {
  SEAL_NOT_FOUND("SEAL001", "해당 지역의 띠부씰을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  ;

  private final String code;
  private final String message;
  private final HttpStatus status;
}
