/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yfive.gbjs.domain.auth.dto.request.LoginRequest;
import com.yfive.gbjs.global.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "인증", description = "인증 관련 API")
@RequestMapping("/api/auth")
public interface AuthController {

  @PostMapping("/login")
  @Operation(summary = "자체 로그인", description = "서비스 자체 로그인을 수행하여 사용자 아이디를 반환합니다.")
  ResponseEntity<ApiResponse<String>> login(
      HttpServletResponse response, @RequestBody @Valid LoginRequest loginRequest);

  @PostMapping("/logout")
  @Operation(summary = "로그아웃", description = "로그아웃을 수행하여 사용자의 Redis RT 삭제 + AT 블랙리스트를 처리합니다.")
  ResponseEntity<ApiResponse<String>> logout(
      HttpServletRequest request, HttpServletResponse response);

  @PostMapping("/refresh")
  @Operation(summary = "액세스 토큰 재발급", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.")
  ResponseEntity<ApiResponse<String>> reissueToken(
      HttpServletRequest request, HttpServletResponse response);

  @PostMapping("/test-login")
  @Operation(summary = "테스트 로그인", description = "시연용 테스트 로그인을 수행하여 사용자 아이디를 반환합니다.")
  ResponseEntity<ApiResponse<String>> testLogin(HttpServletResponse response);
}
