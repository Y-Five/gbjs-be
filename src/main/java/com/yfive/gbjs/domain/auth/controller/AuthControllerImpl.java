/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.yfive.gbjs.domain.auth.dto.request.LoginRequest;
import com.yfive.gbjs.domain.auth.dto.response.TokenResponse;
import com.yfive.gbjs.domain.auth.service.AuthService;
import com.yfive.gbjs.global.common.response.ApiResponse;
import com.yfive.gbjs.global.config.jwt.JwtProvider;

import lombok.RequiredArgsConstructor;

/**
 * 인증 컨트롤러 구현체
 *
 * <p>인증 관련 API 엔드포인트를 제공합니다.
 *
 * @author YFIVE
 * @since 1.0.0
 */
@RestController
@RequiredArgsConstructor
public class AuthControllerImpl implements AuthController {

  private final AuthService authService;
  private final JwtProvider jwtProvider;

  @Override
  public ResponseEntity<ApiResponse<String>> login(
      HttpServletResponse response, @RequestBody @Valid LoginRequest loginRequest) {

    TokenResponse tokenResponse = authService.login(loginRequest);

    jwtProvider.addJwtToCookie(
        response,
        tokenResponse.getRefreshToken(),
        "REFRESH_TOKEN",
        jwtProvider.getExpirationTime(tokenResponse.getRefreshToken()));
    jwtProvider.addJwtToCookie(
        response,
        tokenResponse.getAccessToken(),
        "ACCESS_TOKEN",
        jwtProvider.getExpirationTime(tokenResponse.getAccessToken()));

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(tokenResponse.getAccessToken()));
  }

  @Override
  public ResponseEntity<ApiResponse<String>> logout(
      HttpServletRequest request, HttpServletResponse response) {

    String accessToken = jwtProvider.extractAccessToken(request);
    String result = authService.logout(accessToken);

    jwtProvider.removeJwtCookie(response, "ACCESS_TOKEN");
    jwtProvider.removeJwtCookie(response, "REFRESH_TOKEN");

    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(result));
  }

  @Override
  public ResponseEntity<ApiResponse<String>> reissueToken(
      HttpServletRequest request, HttpServletResponse response) {

    String refreshToken = jwtProvider.extractRefreshToken(request);

    jwtProvider.validateTokenType(refreshToken, "refresh");

    String newAccessToken = authService.reissueAccessToken(refreshToken);

    jwtProvider.addJwtToCookie(
        response, newAccessToken, "ACCESS_TOKEN", jwtProvider.getExpirationTime(newAccessToken));

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(newAccessToken));
  }

  @Override
  public ResponseEntity<ApiResponse<String>> testLogin(HttpServletResponse response) {

    TokenResponse tokenResponse = authService.testLogin();

    jwtProvider.addJwtToCookie(
        response, tokenResponse.getRefreshToken(), "REFRESH_TOKEN", 7 * 24 * 60 * 60);
    jwtProvider.addJwtToCookie(
        response, tokenResponse.getAccessToken(), "ACCESS_TOKEN", 2 * 24 * 60 * 60);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(tokenResponse.getAccessToken()));
  }
}
