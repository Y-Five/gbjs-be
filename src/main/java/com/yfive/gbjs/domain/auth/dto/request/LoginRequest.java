/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "LoginRequest DTO", description = "사용자 로그인을 위한 데이터 전송")
public class LoginRequest {

  @NotBlank(message = "사용자 아이디 항목은 필수입니다.")
  @Schema(description = "사용자 아이디", example = "gbjs")
  private String username;

  @NotBlank(message = "비밀번호 항목은 필수입니다.")
  @Schema(description = "비밀번호", example = "yfive1234!")
  private String password;
}
