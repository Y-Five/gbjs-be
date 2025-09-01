/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** API 응답 공통 형식 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "API 응답 공통 형식")
public class ApiResponse<T> {

  @Schema(description = "응답 코드", example = "SUCCESS")
  private String code;

  @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
  private String message;

  @Schema(description = "응답 데이터")
  private T data;

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(
        ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), data);
  }

  public static <T> ApiResponse<T> success(T data, String message) {
    return new ApiResponse<>(ResponseCode.SUCCESS.getCode(), message, data);
  }

  public static <T> ApiResponse<T> success() {
    return success(null);
  }

  public static <T> ApiResponse<T> error(ResponseCode responseCode) {
    return new ApiResponse<>(responseCode.getCode(), responseCode.getMessage(), null);
  }

  public static <T> ApiResponse<T> error(ResponseCode responseCode, String message) {
    return new ApiResponse<>(responseCode.getCode(), message, null);
  }
}
