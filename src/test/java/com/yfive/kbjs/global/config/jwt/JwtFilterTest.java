/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.kbjs.global.config.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

  @Mock private JwtTokenProvider tokenProvider;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  @Mock private Authentication authentication;

  private TestJwtFilter jwtFilter;

  // JwtFilter의 protected 메서드를 테스트하기 위한 테스트 클래스
  static class TestJwtFilter extends JwtFilter {
    public TestJwtFilter(JwtTokenProvider tokenProvider) {
      super(tokenProvider);
    }

    @Override
    public void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
      super.doFilterInternal(request, response, filterChain);
    }
  }

  @BeforeEach
  void setUp() {
    jwtFilter = new TestJwtFilter(tokenProvider);
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("유효한 JWT 토큰이 있을 경우 인증 정보를 설정한다")
  void doFilterWithValidToken() throws ServletException, IOException {
    // given
    String token = "valid-token";
    given(request.getHeader("Authorization")).willReturn("Bearer " + token);
    given(tokenProvider.validateToken(token)).willReturn(true);
    given(tokenProvider.validateTokenType(token, JwtTokenProvider.TOKEN_TYPE_ACCESS))
        .willReturn(true);
    given(tokenProvider.getAuthentication(token)).willReturn(authentication);

    // when
    jwtFilter.doFilterInternal(request, response, filterChain);

    // then
    verify(filterChain).doFilter(request, response);
    verify(tokenProvider).validateToken(token);
    verify(tokenProvider).validateTokenType(token, JwtTokenProvider.TOKEN_TYPE_ACCESS);
    verify(tokenProvider).getAuthentication(token);
  }

  @Test
  @DisplayName("유효하지 않은 JWT 토큰일 경우 인증 정보를 설정하지 않는다")
  void doFilterWithInvalidToken() throws ServletException, IOException {
    // given
    String token = "invalid-token";
    given(request.getHeader("Authorization")).willReturn("Bearer " + token);
    given(tokenProvider.validateToken(token)).willReturn(false);

    // when
    jwtFilter.doFilterInternal(request, response, filterChain);

    // then
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
    verify(tokenProvider, never()).getAuthentication(anyString());
  }

  @Test
  @DisplayName("Authorization 헤더가 없을 경우 인증 정보를 설정하지 않는다")
  void doFilterWithoutToken() throws ServletException, IOException {
    // given
    given(request.getHeader("Authorization")).willReturn(null);

    // when
    jwtFilter.doFilterInternal(request, response, filterChain);

    // then
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
    verify(tokenProvider, never()).validateToken(anyString());
    verify(tokenProvider, never()).getAuthentication(anyString());
  }
}
