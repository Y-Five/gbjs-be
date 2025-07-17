/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.error.exception.model;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {

  String getCode();

  String getMessage();

  HttpStatus getStatus();
}
