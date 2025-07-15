/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yfive.kbjs.global.common.response.ApiResponse;

/** 테스트용 목 컨트롤러 */
@RestController
public class MockController {

  @GetMapping("/swagger-ui/index.html")
  public ResponseEntity<String> swaggerUi() {
    return ResponseEntity.ok("Swagger UI");
  }

  @GetMapping("/api-docs")
  public ResponseEntity<String> apiDocs() {
    return ResponseEntity.ok("API Docs");
  }

  @GetMapping("/api/auth/test-login")
  public ResponseEntity<ApiResponse<String>> mockLoginPage() {
    return ResponseEntity.ok(ApiResponse.success("테스트 로그인 페이지"));
  }
}
