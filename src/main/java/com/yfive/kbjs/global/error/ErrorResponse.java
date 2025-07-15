/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.error;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;

import com.yfive.gbjs.global.common.response.ResponseCode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "에러 응답")
public class ErrorResponse {

  @Schema(description = "에러 발생 시간", example = "2025-06-01T12:00:00")
  private final LocalDateTime timestamp;

  @Schema(description = "에러 코드", example = "BAD_REQUEST")
  private final String code;

  @Schema(description = "에러 메시지", example = "잘못된 요청입니다.")
  private final String message;

  @Schema(description = "상세 에러 메시지 목록")
  private final List<FieldError> errors;

  @Schema(description = "에러 발생 경로", example = "/api/users")
  private final String path;

  @Getter
  @Builder
  public static class FieldError {
    @Schema(description = "필드명", example = "username")
    private final String field;

    @Schema(description = "에러 메시지", example = "사용자 이름은 필수입니다.")
    private final String message;

    @Schema(description = "거부된 값", example = "null")
    private final String rejectedValue;
  }

  public static ResponseEntity<ErrorResponse> toResponseEntity(
      ResponseCode responseCode, String path) {
    return ResponseEntity.status(responseCode.getHttpStatus())
        .body(
            ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .code(responseCode.getCode())
                .message(responseCode.getMessage())
                .errors(new ArrayList<>())
                .path(path)
                .build());
  }

  public static ResponseEntity<ErrorResponse> toResponseEntity(
      ResponseCode responseCode, String path, List<FieldError> errors) {
    return ResponseEntity.status(responseCode.getHttpStatus())
        .body(
            ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .code(responseCode.getCode())
                .message(responseCode.getMessage())
                .errors(errors)
                .path(path)
                .build());
  }
}
