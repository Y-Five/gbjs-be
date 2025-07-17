package com.yfive.gbjs.domain.weather.exception;

import com.yfive.gbjs.global.error.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum WeatherErrorStatus implements BaseErrorCode {

  EMPTY_RESPONSE("WEATHER001", "날씨 API 응답이 비어있습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  ITEM_NOT_FOUND("WEATHER002", "날씨 정보가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
  PARSING_ERROR("WEATHER003", "날씨 정보 파싱 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  API_REQUEST_FAILED("WEATHER004", "날씨 API 요청 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
