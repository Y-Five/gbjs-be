/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.exception;

import org.springframework.http.HttpStatus;

import com.yfive.gbjs.global.error.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SealErrorStatus implements BaseErrorCode {
  SEAL_NOT_FOUND("SEAL001", "해당 지역의 띠부씰을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  USER_SEAL_NOT_FOUND("SEAL002", "사용자가 획득한 띠부씰을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  SEAL_TOO_FAR_GENERAL("SEAL003", "띠부씰 획득에 실패했습니다. 500m 이내로 가까이 가주세요.", HttpStatus.BAD_REQUEST),
  SEAL_TOO_FAR_ULLUNG("SEAL004", "띠부씰 획득에 실패했습니다. 2km 이내로 가까이 가주세요.", HttpStatus.BAD_REQUEST),
  SEAL_ALREADY_COLLECTED("SEAL005", "이미 획득한 띠부씰입니다.", HttpStatus.BAD_REQUEST),
  SEAL_LOCATION_INFO_MISSING("SEAL006", "띠부씰의 위치 정보가 없습니다.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
