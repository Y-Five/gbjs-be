/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.error.exception;

import com.yfive.gbjs.global.common.response.ResponseCode;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

  private final ResponseCode responseCode;

  public BusinessException(ResponseCode responseCode) {
    super(responseCode.getMessage());
    this.responseCode = responseCode;
  }

  public BusinessException(ResponseCode responseCode, String message) {
    super(message);
    this.responseCode = responseCode;
  }

  public BusinessException(ResponseCode responseCode, String message, Throwable cause) {
    super(message, cause);
    this.responseCode = responseCode;
  }
}
