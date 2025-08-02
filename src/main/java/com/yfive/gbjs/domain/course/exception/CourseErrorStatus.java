/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.exception;

import org.springframework.http.HttpStatus;

import com.yfive.gbjs.global.error.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CourseErrorStatus implements BaseErrorCode {
  _COURSE_NOT_FOUND("COURSE404", "코스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  _INVALID_DATE_RANGE("COURSE400", "잘못된 날짜 범위입니다.", HttpStatus.BAD_REQUEST),
  _NO_SPOTS_AVAILABLE("COURSE404", "해당 지역에 이용 가능한 관광지가 없습니다.", HttpStatus.NOT_FOUND),
  _SPOT_NOT_FOUND("COURSE404", "관광지를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  _INVALID_LOCATION("COURSE400", "잘못된 지역명입니다.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
