/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.kbjs.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yfive.kbjs.domain.user.dto.response.ProtectedResourceResponse;
import com.yfive.kbjs.global.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 보호된 리소스 컨트롤러
 *
 * <p>인증이 필요한 API 엔드포인트를 제공합니다.
 *
 * @author YFIVE
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/protected")
@RequiredArgsConstructor
@Tag(name = "보호된 API", description = "인증이 필요한 API")
public class ProtectedController {

  /**
   * 보호된 리소스 조회
   *
   * <p>인증된 사용자만 접근할 수 있는 리소스를 조회합니다.
   *
   * @return 보호된 리소스 정보
   */
  @GetMapping
  @Operation(
      summary = "보호된 리소스 조회",
      description = "인증된 사용자만 접근 가능한 API",
      security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<ApiResponse<ProtectedResourceResponse>> getProtectedResource() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    ProtectedResourceResponse response =
        ProtectedResourceResponse.builder()
            .message("인증된 사용자만 볼 수 있는 정보입니다.")
            .username(auth.getName())
            .authorities(auth.getAuthorities())
            .build();

    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
