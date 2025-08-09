/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.common.exception;

import org.springframework.http.HttpStatus;

import com.yfive.gbjs.global.error.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PageErrorStatus implements BaseErrorCode {
  PAGE_NOT_FOUND("PAGE001", "페이지가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
  PAGE_SIZE_ERROR("PAGE002", "페이지 크기 오류가 발생했습니다.", HttpStatus.BAD_REQUEST),
  PAGING_ERROR("PAGE003", "페이징 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
