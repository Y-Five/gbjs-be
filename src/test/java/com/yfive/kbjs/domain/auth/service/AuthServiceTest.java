/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.kbjs.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.yfive.kbjs.domain.auth.dto.request.LoginRequest;
import com.yfive.kbjs.domain.auth.dto.request.TokenRefreshRequest;
import com.yfive.kbjs.domain.auth.dto.response.TokenResponse;
import com.yfive.kbjs.global.config.jwt.JwtTokenProvider;
import com.yfive.kbjs.global.error.exception.InvalidTokenException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private JwtTokenProvider jwtTokenProvider;

  @InjectMocks private AuthServiceImpl authService;

  @Test
  @DisplayName("로그인 및 토큰 발급 테스트")
  void loginTest() {
    // given
    LoginRequest loginRequest = new LoginRequest("testuser", "password");

    TokenResponse expectedResponse =
        TokenResponse.builder()
            .accessToken("test-access-token")
            .refreshToken("test-refresh-token")
            .username(loginRequest.username())
            .build();

    when(jwtTokenProvider.createTokens(any(Authentication.class))).thenReturn(expectedResponse);

    // when
    TokenResponse tokenResponse = authService.login(loginRequest);

    // then
    assertThat(tokenResponse).isNotNull();
    assertThat(tokenResponse.getAccessToken()).isEqualTo(expectedResponse.getAccessToken());
    assertThat(tokenResponse.getRefreshToken()).isEqualTo(expectedResponse.getRefreshToken());
    assertThat(tokenResponse.getUsername()).isEqualTo(expectedResponse.getUsername());
  }

  @Test
  @DisplayName("토큰 갱신 테스트")
  void refreshTokenTest() {
    // given
    String username = "testuser";
    String refreshToken = "valid-refresh-token";
    String newAccessToken = "new-access-token";
    TokenRefreshRequest refreshRequest = new TokenRefreshRequest(refreshToken);

    when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
    when(jwtTokenProvider.validateTokenType(refreshToken, JwtTokenProvider.TOKEN_TYPE_REFRESH))
        .thenReturn(true);
    when(jwtTokenProvider.getUsernameFromToken(refreshToken)).thenReturn(username);
    when(jwtTokenProvider.validateRefreshToken(username, refreshToken)).thenReturn(true);
    when(jwtTokenProvider.createToken(
            any(Authentication.class), eq(JwtTokenProvider.TOKEN_TYPE_ACCESS)))
        .thenReturn(newAccessToken);

    // when
    TokenResponse tokenResponse = authService.refreshToken(refreshRequest);

    // then
    assertThat(tokenResponse).isNotNull();
    assertThat(tokenResponse.getAccessToken()).isEqualTo(newAccessToken);
    assertThat(tokenResponse.getRefreshToken()).isEqualTo(refreshToken);
    assertThat(tokenResponse.getUsername()).isEqualTo(username);
  }

  @Test
  @DisplayName("잘못된 리프레시 토큰 테스트")
  void invalidRefreshTokenTest() {
    // given
    String invalidToken = "invalid-token";
    TokenRefreshRequest invalidRequest = new TokenRefreshRequest(invalidToken);
    when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

    // when & then
    assertThrows(InvalidTokenException.class, () -> authService.refreshToken(invalidRequest));
  }

  @Test
  @DisplayName("액세스 토큰으로 리프레시 시도 테스트")
  void wrongTokenTypeTest() {
    // given
    String accessToken = "valid-access-token";
    TokenRefreshRequest wrongTypeRequest = new TokenRefreshRequest(accessToken);

    when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
    when(jwtTokenProvider.validateTokenType(accessToken, JwtTokenProvider.TOKEN_TYPE_REFRESH))
        .thenReturn(false);

    // when & then
    assertThrows(InvalidTokenException.class, () -> authService.refreshToken(wrongTypeRequest));
  }
}
