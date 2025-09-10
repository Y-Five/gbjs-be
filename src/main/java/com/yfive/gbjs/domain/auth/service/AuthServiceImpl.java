/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yfive.gbjs.domain.auth.dto.request.LoginRequest;
import com.yfive.gbjs.domain.auth.dto.response.TokenResponse;
import com.yfive.gbjs.domain.auth.exception.AuthErrorStatus;
import com.yfive.gbjs.domain.user.entity.User;
import com.yfive.gbjs.domain.user.exception.UserErrorStatus;
import com.yfive.gbjs.domain.user.repository.UserRepository;
import com.yfive.gbjs.global.config.jwt.JwtProvider;
import com.yfive.gbjs.global.error.exception.CustomException;

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

  @Value("${gbjs.test.username}")
  private String testUsername;

  @Value("${gbjs.test.password}")
  private String testPassword;

  private final AuthenticationManager authenticationManager;
  private final JwtProvider jwtProvider;
  private final UserRepository userRepository;
  private final UserDetailsService userDetailsService;

  @Override
  @Transactional
  public TokenResponse login(LoginRequest loginRequest) {
    User user =
        userRepository
            .findByUsername(loginRequest.getUsername())
            .orElseThrow(() -> new CustomException(UserErrorStatus.USER_NOT_FOUND));

    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(
            loginRequest.getUsername(), loginRequest.getPassword());

    try {
      // 인증 시도
      Authentication authenticated = authenticationManager.authenticate(authenticationToken);

      // JWT 발급
      TokenResponse tokenResponse = jwtProvider.createTokens(authenticated);
      log.info("로그인 성공: {}", user.getUsername());
      return tokenResponse;

    } catch (Exception e) {
      throw new CustomException(AuthErrorStatus.LOGIN_FAIL);
    }
  }

  @Override
  public String logout(String accessToken) {
    String username = jwtProvider.getUsernameFromToken(accessToken);

    jwtProvider.deleteRefreshToken(username);
    jwtProvider.blacklistToken(accessToken);

    log.info("로그아웃 성공: {}", username);
    return "로그아웃 성공 - 사용자: " + username;
  }

  @Override
  public String reissueAccessToken(String refreshToken) {
    if (!jwtProvider.validateToken(refreshToken)
        || !jwtProvider.validateTokenType(refreshToken, JwtProvider.TOKEN_TYPE_REFRESH)) {
      throw new CustomException(AuthErrorStatus.INVALID_REFRESH_TOKEN);
    }
    String username = jwtProvider.getUsernameFromToken(refreshToken);
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new CustomException(UserErrorStatus.USER_NOT_FOUND));

    if (!jwtProvider.validateRefreshToken(user.getUsername(), refreshToken)) {
      throw new CustomException(AuthErrorStatus.INVALID_REFRESH_TOKEN);
    }

    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

    log.info("AT 재발급 성공: {}", user.getUsername());
    return jwtProvider.createToken(authentication);
  }

  @Override
  @Transactional
  public TokenResponse testLogin() {

    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(testUsername, testPassword);

    try {
      Authentication authenticated = authenticationManager.authenticate(authenticationToken);

      TokenResponse tokenResponse = jwtProvider.createTokens(authenticated);
      log.info("테스트 로그인 성공: {}", testUsername);
      return tokenResponse;
    } catch (Exception e) {
      // 인증 실패 시 커스텀 예외로 변환
      throw new CustomException(AuthErrorStatus.LOGIN_FAIL);
    }
  }
}
