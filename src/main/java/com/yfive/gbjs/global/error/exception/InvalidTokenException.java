/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.error.exception;

import com.yfive.gbjs.global.common.response.ResponseCode;

public class InvalidTokenException extends BusinessException {

  public InvalidTokenException(String message) {
    super(ResponseCode.INVALID_TOKEN, message);
  }

  public InvalidTokenException() {
    super(ResponseCode.INVALID_TOKEN);
  }
}
