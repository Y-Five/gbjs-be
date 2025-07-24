/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.user.exception;

import org.springframework.http.HttpStatus;

import com.yfive.gbjs.global.error.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserErrorStatus implements BaseErrorCode {
  USER_NOT_FOUND("USER001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  EXIST_NICKNAME("USER002", "이미 존재하는 닉네임입니다.", HttpStatus.BAD_REQUEST),
  UNAUTHORIZED("USER003", "인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED),
  ;

  private final String code;
  private final String message;
  private final HttpStatus status;
}
