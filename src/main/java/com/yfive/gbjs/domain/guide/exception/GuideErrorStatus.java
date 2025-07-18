/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.exception;

import org.springframework.http.HttpStatus;

import com.yfive.gbjs.global.error.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GuideErrorStatus implements BaseErrorCode {
  EMPTY_RESPONSE("GUIDE001", "가이드 API 응답이 비어있습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  ITEM_NOT_FOUND("GUIDE002", "가이드 정보가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
  PARSING_ERROR("GUIDE003", "가이드 정보 파싱 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  API_REQUEST_FAILED("GUIDE004", "가이드 API 요청 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
