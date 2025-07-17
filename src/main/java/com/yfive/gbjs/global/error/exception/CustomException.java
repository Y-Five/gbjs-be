/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.error.exception;

import com.yfive.gbjs.global.error.exception.model.BaseErrorCode;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

  private final BaseErrorCode errorCode;

  public CustomException(BaseErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
