/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.auth.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yfive.gbjs.domain.auth.dto.request.LoginRequest;
import com.yfive.gbjs.domain.auth.dto.request.TokenRefreshRequest;
import com.yfive.gbjs.domain.auth.dto.response.TokenResponse;
import com.yfive.gbjs.global.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "인증", description = "인증 관련 API")
@RequestMapping("/api/auth")
public interface AuthController {

  @GetMapping("/login")
  @Operation(summary = "로그인 페이지", description = "로그인 페이지 테스트용 API")
  ResponseEntity<ApiResponse<String>> loginPage();

  @PostMapping("/login")
  @Operation(summary = "로그인", description = "사용자 로그인 및 JWT 토큰 발급 (Access Token, Refresh Token)")
  ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest loginRequest);

  @PostMapping("/refresh")
  @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token 발급")
  ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
      @Valid @RequestBody TokenRefreshRequest refreshRequest);

  @PostMapping("/logout")
  @Operation(summary = "로그아웃", description = "사용자 로그아웃 및 토큰 무효화")
  ResponseEntity<ApiResponse<Void>> logout(
      @RequestHeader(value = "Authorization", required = false) String bearerToken);
}
