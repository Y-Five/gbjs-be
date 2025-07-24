/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yfive.gbjs.global.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "사용자", description = "사용자 관련 API")
@RequestMapping("/api/users")
public interface UserController {

  @PutMapping("/email-marketing-consent")
  @Operation(
      summary = "이메일 수신 동의 토글",
      description = "현재 로그인한 사용자의 이메일 마케팅 수신 여부를 반전시킵니다. (true → false, false → true)")
  ResponseEntity<ApiResponse<Boolean>> updateEmailMarketingConsent();

  @PutMapping("/push-notification-consent")
  @Operation(
      summary = "푸시 알림 수신 동의 토글",
      description = "현재 로그인한 사용자의 푸시 알림 수신 여부를 반전시킵니다. (true → false, false → true)")
  ResponseEntity<ApiResponse<Boolean>> updatePushNotificationConsent();

  @PutMapping("/location-consent")
  @Operation(
      summary = "위치 정보 제공 동의 토글",
      description = "현재 로그인한 사용자의 위치 정보 제공 동의 여부를 반전시킵니다. (true → false, false → true)")
  ResponseEntity<ApiResponse<Boolean>> updateLocationConsent();

import org.springframework.web.bind.annotation.RequestParam;

  @PutMapping("/nickname")
  @Operation(summary = "닉네임 수정", description = "현재 로그인한 사용자의 닉네임을 수정합니다.")
  ResponseEntity<ApiResponse<String>> updateNickname(@RequestParam String newNickname);

  @DeleteMapping
  @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 계정을 삭제합니다. (Hard Delete)")
  ResponseEntity<ApiResponse<Void>> deleteUser();
}
