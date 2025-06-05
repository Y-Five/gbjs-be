/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.kbjs.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yfive.kbjs.domain.user.dto.response.ProtectedResourceResponse;
import com.yfive.kbjs.global.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "보호된 API", description = "인증이 필요한 API")
@RequestMapping("/api/protected")
public interface ProtectedController {

  @GetMapping
  @Operation(
      summary = "보호된 리소스 조회",
      description = "인증된 사용자만 접근 가능한 API",
      security = @SecurityRequirement(name = "bearerAuth"))
  ResponseEntity<ApiResponse<ProtectedResourceResponse>> getProtectedResource();
}
