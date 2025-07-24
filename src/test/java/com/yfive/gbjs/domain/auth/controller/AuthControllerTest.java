/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.auth.controller;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfive.gbjs.domain.auth.dto.request.LoginRequest;
import com.yfive.gbjs.domain.auth.dto.request.TokenRefreshRequest;
import com.yfive.gbjs.domain.auth.dto.response.TokenResponse;
import com.yfive.gbjs.domain.auth.service.AuthService;
import com.yfive.gbjs.global.config.MockRedisConfig;
import com.yfive.gbjs.global.config.SecurityConfig;
import com.yfive.gbjs.global.config.TestJwtPropertiesConfig;
import com.yfive.gbjs.global.config.jwt.JwtTokenProvider;
import com.yfive.gbjs.global.error.exception.InvalidTokenException;
import com.yfive.gbjs.global.security.CustomOAuth2UserService;
import com.yfive.gbjs.global.security.OAuth2LoginSuccessHandler;

@WebMvcTest(AuthControllerImpl.class)
@Import({SecurityConfig.class, TestJwtPropertiesConfig.class, MockRedisConfig.class})
@ActiveProfiles("test")
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private AuthService authService;

  @MockitoBean private JwtTokenProvider jwtTokenProvider;

  @MockitoBean private CustomOAuth2UserService customOAuth2UserService;

  @MockitoBean private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

  @Test
  @DisplayName("로그인 API 테스트")
  void loginApiTest() throws Exception {
    // given
    LoginRequest loginRequest = new LoginRequest("testuser", "password");
    TokenResponse tokenResponse =
        TokenResponse.builder()
            .accessToken("test-access-token")
            .refreshToken("test-refresh-token")
            .username(loginRequest.username())
            .build();

    when(authService.login(any(LoginRequest.class))).thenReturn(tokenResponse);

    String content = objectMapper.writeValueAsString(loginRequest);

    // when
    ResultActions result =
        mockMvc.perform(
            post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(content));

    // then
    result
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is("SUCCESS")))
        .andExpect(jsonPath("$.data", notNullValue()))
        .andExpect(jsonPath("$.data", hasKey("accessToken")))
        .andExpect(jsonPath("$.data", hasKey("refreshToken")))
        .andExpect(jsonPath("$.data", hasKey("username")))
        .andExpect(jsonPath("$.data.username", is(loginRequest.username())));
  }

  @Test
  @DisplayName("토큰 갱신 API 테스트")
  void refreshTokenApiTest() throws Exception {
    // given
    String username = "testuser";
    String refreshToken = "test-refresh-token";

    TokenRefreshRequest refreshRequest = new TokenRefreshRequest(refreshToken);
    TokenResponse tokenResponse =
        TokenResponse.builder()
            .accessToken("new-access-token")
            .refreshToken(refreshToken)
            .username(username)
            .build();

    when(authService.refreshToken(any(TokenRefreshRequest.class))).thenReturn(tokenResponse);

    String content = objectMapper.writeValueAsString(refreshRequest);

    // when
    ResultActions result =
        mockMvc.perform(
            post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(content));

    // then
    result
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is("SUCCESS")))
        .andExpect(jsonPath("$.data", notNullValue()))
        .andExpect(jsonPath("$.data", hasKey("accessToken")))
        .andExpect(jsonPath("$.data", hasKey("refreshToken")))
        .andExpect(jsonPath("$.data", hasKey("username")))
        .andExpect(jsonPath("$.data.username", is(username)));
  }

  @Test
  @DisplayName("잘못된 리프레시 토큰 API 테스트")
  void invalidRefreshTokenApiTest() throws Exception {
    // given
    TokenRefreshRequest invalidRequest = new TokenRefreshRequest("invalid-token");
    when(authService.refreshToken(any(TokenRefreshRequest.class)))
        .thenThrow(new InvalidTokenException("Invalid refresh token"));

    String content = objectMapper.writeValueAsString(invalidRequest);

    // when
    ResultActions result =
        mockMvc.perform(
            post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(content));

    // then
    result
        .andDo(print())
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code", is("INVALID_TOKEN")));
  }
}
