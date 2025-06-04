/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.kbjs.global.config.jwt;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtFilter extends OncePerRequestFilter {

  public static final String AUTHORIZATION_HEADER = "Authorization";
  public static final String BEARER_PREFIX = "Bearer ";

  private final JwtTokenProvider tokenProvider;

  /**
   * 생성자
   *
   * @param tokenProvider JWT 토큰 제공자
   */
  public JwtFilter(JwtTokenProvider tokenProvider) {
    this.tokenProvider = tokenProvider;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String jwt = resolveToken(request);

    if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
      // Access Token인지 확인
      if (tokenProvider.validateTokenType(jwt, JwtTokenProvider.TOKEN_TYPE_ACCESS)) {
        Authentication authentication = tokenProvider.getAuthentication(jwt);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Security Context에 '{}' 인증 정보를 저장했습니다", authentication.getName());
      } else {
        log.debug("유효한 Access Token이 아닙니다");
      }
    } else {
      log.debug("유효한 JWT 토큰이 없습니다");
    }

    filterChain.doFilter(request, response);
  }

  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
