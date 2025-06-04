/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.kbjs.global.error.exception;

import com.yfive.kbjs.global.common.response.ResponseCode;

public class InvalidTokenException extends BusinessException {

  public InvalidTokenException(String message) {
    super(ResponseCode.INVALID_TOKEN, message);
  }

  public InvalidTokenException() {
    super(ResponseCode.INVALID_TOKEN);
  }
}
