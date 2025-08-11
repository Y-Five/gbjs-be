/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "LoginResponse DTO", description = "사용자 로그인에 대한 응답 반환")
public class LoginResponse {

  @Schema(description = "사용자 식별자", example = "1")
  private Long userId;

  @Schema(description = "소셜 로그인 이메일", example = "unijun0109@gmail.com")
  private String email;

  @Schema(description = "Access Token")
  private String accessToken;

  @Schema(description = "Access Token 만료 시간", example = "1800000")
  private Long expirationTime;
}
