/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.auth.service;

import com.yfive.gbjs.domain.auth.dto.request.LoginRequest;
import com.yfive.gbjs.domain.auth.dto.request.TokenRefreshRequest;
import com.yfive.gbjs.domain.auth.dto.response.TokenResponse;

/**
 * 인증 서비스 인터페이스
 *
 * <p>로그인, 토큰 갱신, 로그아웃 등의 인증 관련 기능을 정의합니다.
 *
 * @author YFIVE
 * @since 1.0.0
 */
public interface AuthService {

  /**
   * 로그인 처리
   *
   * <p>사용자 인증 후 JWT 토큰을 발급합니다.
   *
   * @param loginRequest 로그인 요청 정보
   * @return 토큰 응답 객체
   */
  TokenResponse login(LoginRequest loginRequest);

  /**
   * 토큰 갱신
   *
   * <p>Refresh Token을 사용하여 새로운 Access Token을 발급합니다.
   *
   * @param refreshRequest 토큰 갱신 요청 정보
   * @return 갱신된 토큰 응답 객체
   */
  TokenResponse refreshToken(TokenRefreshRequest refreshRequest);

  /**
   * 로그아웃 처리
   *
   * <p>사용자 로그아웃 시 Access Token을 블랙리스트에 추가하고, Refresh Token을 삭제합니다.
   *
   * @param accessToken 로그아웃할 Access Token
   */
  void logout(String accessToken);
}
