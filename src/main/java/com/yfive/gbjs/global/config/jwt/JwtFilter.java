/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.config.jwt;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

  private final JwtProvider jwtProvider;
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if ("/error".equals(request.getRequestURI())
        || request.getRequestURI().startsWith("/.well-known/")) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String token = jwtProvider.extractAccessToken(request);

      if (token != null
          && jwtProvider.validateToken(token)
          && jwtProvider.validateTokenType(token, JwtProvider.TOKEN_TYPE_ACCESS)) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
          String username = jwtProvider.getUsernameFromToken(token);
          UserDetails userDetails = userDetailsService.loadUserByUsername(username);

          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      }
      // 토큰이 없는 경우, 로그 쌓지 않고 다음 필터로 진행
    } catch (JwtException | IllegalArgumentException e) {
      SecurityContextHolder.clearContext();
      throw new BadCredentialsException("유효하지 않은 JWT 토큰", e);
    }

    filterChain.doFilter(request, response);
  }
}
