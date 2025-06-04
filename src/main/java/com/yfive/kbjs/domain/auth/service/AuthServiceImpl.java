/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.kbjs.domain.auth.service;

import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import com.yfive.kbjs.domain.auth.dto.request.LoginRequest;
import com.yfive.kbjs.domain.auth.dto.request.TokenRefreshRequest;
import com.yfive.kbjs.domain.auth.dto.response.TokenResponse;
import com.yfive.kbjs.global.config.jwt.JwtTokenProvider;
import com.yfive.kbjs.global.config.jwt.TokenRepository;
import com.yfive.kbjs.global.error.exception.InvalidTokenException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 인증 서비스 구현 클래스
 *
 * <p>인증 관련 비즈니스 로직을 처리합니다. JWT 토큰 발급, 갱신, 로그아웃 등의 기능을 제공합니다.
 *
 * @author YFIVE
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  /** JWT 토큰 제공자 */
  private final JwtTokenProvider jwtTokenProvider;

  /** 토큰 저장소 */
  private final TokenRepository tokenRepository;

  /** {@inheritDoc} */
  @Override
  public TokenResponse login(LoginRequest loginRequest) {
    // 실제 애플리케이션에서는 사용자 인증 로직 구현 필요
    log.info("로그인 시도: {}", loginRequest.username());

    // 테스트용 인증 객체 생성
    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
    User principal = new User(loginRequest.username(), "", Collections.singleton(authority));
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(principal, "", Collections.singleton(authority));

    // Access Token과 Refresh Token 생성 (Redis에 저장됨)
    return jwtTokenProvider.createTokens(authentication);
  }

  /** {@inheritDoc} */
  @Override
  public TokenResponse refreshToken(TokenRefreshRequest refreshRequest) {
    String refreshToken = refreshRequest.refreshToken();

    // Refresh Token 유효성 검증
    if (!jwtTokenProvider.validateToken(refreshToken)) {
      throw new InvalidTokenException("유효하지 않은 Refresh Token입니다.");
    }

    // Refresh Token 타입 검증
    if (!jwtTokenProvider.validateTokenType(refreshToken, JwtTokenProvider.TOKEN_TYPE_REFRESH)) {
      throw new InvalidTokenException("유효한 Refresh Token이 아닙니다.");
    }

    // 토큰에서 사용자 정보 추출
    String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

    // Redis에 저장된 Refresh Token과 비교
    if (!jwtTokenProvider.validateRefreshToken(username, refreshToken)) {
      throw new InvalidTokenException("저장된 Refresh Token과 일치하지 않습니다.");
    }

    // 테스트용 인증 객체 생성 (실제로는 사용자 정보를 DB에서 조회해야 함)
    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
    User principal = new User(username, "", Collections.singleton(authority));
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(principal, "", Collections.singleton(authority));

    // 새로운 Access Token 생성 (Refresh Token은 재사용)
    String newAccessToken =
        jwtTokenProvider.createToken(authentication, JwtTokenProvider.TOKEN_TYPE_ACCESS);

    // TokenResponse 객체로 변환하여 반환
    return TokenResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(refreshToken) // 기존 Refresh Token 유지
        .username(username)
        .build();
  }

  /** {@inheritDoc} */
  @Override
  public void logout(String accessToken) {
    if (accessToken != null && !accessToken.isEmpty()) {
      // 토큰에서 사용자 정보 추출
      String username = jwtTokenProvider.getUsernameFromToken(accessToken);

      // Access Token 블랙리스트에 추가
      jwtTokenProvider.blacklistToken(accessToken);

      // Refresh Token 삭제
      tokenRepository.deleteRefreshToken(username);

      log.info("로그아웃 처리 완료: {}", username);
    }
  }
}
