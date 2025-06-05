/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.kbjs.domain.auth.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.yfive.kbjs.domain.auth.dto.request.LoginRequest;
import com.yfive.kbjs.domain.auth.dto.request.TokenRefreshRequest;
import com.yfive.kbjs.domain.auth.dto.response.TokenResponse;
import com.yfive.kbjs.domain.auth.service.AuthService;
import com.yfive.kbjs.global.common.response.ApiResponse;
import com.yfive.kbjs.global.config.jwt.JwtFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 인증 컨트롤러 구현체
 *
 * <p>인증 관련 API 엔드포인트를 제공합니다.
 *
 * @author YFIVE
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthControllerImpl implements AuthController {

  /** 인증 서비스 */
  private final AuthService authService;

  /**
   * 로그인 페이지 테스트용 API
   *
   * @return 로그인 페이지 메시지
   */
  @Override
  public ResponseEntity<ApiResponse<String>> loginPage() {
    return ResponseEntity.ok(ApiResponse.success("로그인 페이지"));
  }

  /**
   * 로그인 API
   *
   * <p>사용자 로그인 처리 후 JWT 토큰을 발급합니다.
   *
   * @param loginRequest 로그인 요청 정보
   * @return 토큰 정보가 포함된 응답
   */
  @Override
  public ResponseEntity<ApiResponse<TokenResponse>> login(
      @Valid @RequestBody LoginRequest loginRequest) {
    TokenResponse tokenResponse = authService.login(loginRequest);
    return ResponseEntity.ok(ApiResponse.success(tokenResponse));
  }

  /**
   * 토큰 갱신 API
   *
   * <p>Refresh Token을 사용하여 새로운 Access Token을 발급합니다.
   *
   * @param refreshRequest 토큰 갱신 요청 정보
   * @return 갱신된 토큰 정보가 포함된 응답
   */
  @Override
  public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
      @Valid @RequestBody TokenRefreshRequest refreshRequest) {
    TokenResponse tokenResponse = authService.refreshToken(refreshRequest);
    return ResponseEntity.ok(ApiResponse.success(tokenResponse));
  }

  /**
   * 로그아웃 API
   *
   * <p>사용자 로그아웃 처리 및 토큰 무효화를 수행합니다.
   *
   * @param bearerToken Authorization 헤더에 포함된 Bearer 토큰
   * @return 성공 응답
   */
  @Override
  public ResponseEntity<ApiResponse<Void>> logout(
      @RequestHeader(value = "Authorization", required = false) String bearerToken) {
    String token = resolveToken(bearerToken);
    authService.logout(token);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  /**
   * Bearer 토큰에서 액세스 토큰 추출
   *
   * @param bearerToken Bearer 접두사가 포함된 토큰
   * @return 추출된 액세스 토큰 또는 null
   */
  private String resolveToken(String bearerToken) {
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtFilter.BEARER_PREFIX)) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
