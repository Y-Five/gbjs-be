/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.kbjs.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import com.yfive.kbjs.domain.user.dto.response.ProtectedResourceResponse;
import com.yfive.kbjs.global.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 보호된 리소스 컨트롤러 구현체
 *
 * <p>인증이 필요한 API 엔드포인트를 구현합니다.
 *
 * @author YFIVE
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ProtectedControllerImpl implements ProtectedController {

  /**
   * 보호된 리소스 조회
   *
   * <p>인증된 사용자만 접근할 수 있는 리소스를 조회합니다.
   *
   * @return 보호된 리소스 정보
   */
  @Override
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
