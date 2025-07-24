/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.security;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.yfive.gbjs.domain.auth.dto.response.TokenResponse;
import com.yfive.gbjs.domain.user.entity.User;
import com.yfive.gbjs.domain.user.exception.UserErrorStatus;
import com.yfive.gbjs.domain.user.repository.UserRepository;
import com.yfive.gbjs.global.config.jwt.JwtTokenProvider;
import com.yfive.gbjs.global.error.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtTokenProvider jwtProvider;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");

    if (kakaoAccount == null || !kakaoAccount.containsKey("email")) {
      throw new CustomException(UserErrorStatus.UNAUTHORIZED);
    }

    String email = (String) kakaoAccount.get("email");

    User user =
        userRepository
            .findByUsername(email)
            .orElseThrow(() -> new CustomException(UserErrorStatus.USER_NOT_FOUND));

    TokenResponse tokenResponse = jwtProvider.createTokens(authentication);
    jwtProvider.addJwtToCookie(
        response,
        tokenResponse.getRefreshToken(),
        "REFRESH_TOKEN",
        jwtProvider.getExpirationTime(tokenResponse.getRefreshToken()));
    jwtProvider.addJwtToCookie(
        response,
        tokenResponse.getAccessToken(),
        "ACCESS_TOKEN",
        jwtProvider.getExpirationTime(tokenResponse.getRefreshToken()));

    log.info("카카오 로그인 성공: {}", user.getUsername());

    response.addHeader("Authorization", "Bearer " + tokenResponse.getAccessToken());
    response.sendRedirect("/swagger-ui/index.html#/");
  }
}
