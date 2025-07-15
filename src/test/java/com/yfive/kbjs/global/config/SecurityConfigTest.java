/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.yfive.gbjs.domain.auth.controller.AuthControllerImpl;
import com.yfive.gbjs.domain.auth.controller.MockController;
import com.yfive.gbjs.domain.auth.service.AuthService;
import com.yfive.gbjs.domain.user.controller.ProtectedController;
import com.yfive.kbjs.global.config.jwt.JwtTokenProvider;
import com.yfive.kbjs.global.config.jwt.TokenRepository;

@WebMvcTest(
    controllers = {AuthControllerImpl.class, ProtectedController.class, MockController.class})
@Import({SecurityConfig.class, TestJwtPropertiesConfig.class, MockRedisConfig.class})
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private JwtTokenProvider jwtTokenProvider;

  @MockitoBean private AuthService authService;

  @MockitoBean private TokenRepository tokenRepository;

  @Test
  @DisplayName("인증되지 않은 사용자는 보호된 엔드포인트에 접근할 수 없다")
  void unauthenticatedUserCannotAccessProtectedEndpoints() throws Exception {
    mockMvc.perform(get("/api/protected")).andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("모든 사용자는 Swagger UI에 접근할 수 있다")
  void allUsersCanAccessSwaggerUI() throws Exception {
    mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("모든 사용자는 인증 API에 접근할 수 있다")
  void allUsersCanAccessAuthEndpoints() throws Exception {
    mockMvc.perform(get("/api/auth/test-login")).andExpect(status().isOk());
  }
}
