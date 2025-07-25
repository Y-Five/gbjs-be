/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.exception;

import com.yfive.gbjs.global.error.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CourseErrorStatus implements BaseErrorCode {
  COURSE_NOT_FOUND("COURSE001", "코스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  ;

  private final String code;
  private final String message;
  private final HttpStatus status;
}