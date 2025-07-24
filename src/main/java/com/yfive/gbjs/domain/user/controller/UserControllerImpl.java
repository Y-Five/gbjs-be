/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.yfive.gbjs.domain.user.service.UserService;
import com.yfive.gbjs.global.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserControllerImpl implements UserController {

  private final UserService userService;

  @Override
  public ResponseEntity<ApiResponse<Boolean>> updateEmailMarketingConsent() {
    return ResponseEntity.ok(ApiResponse.success(userService.toggleEmailMarketingConsent()));
  }

  @Override
  public ResponseEntity<ApiResponse<Boolean>> updatePushNotificationConsent() {
    return ResponseEntity.ok(ApiResponse.success(userService.togglePushNotificationConsent()));
  }

  @Override
  public ResponseEntity<ApiResponse<Boolean>> updateLocationConsent() {
    return ResponseEntity.ok(ApiResponse.success(userService.toggleLocationConsent()));
  }

  @Override
  public ResponseEntity<ApiResponse<String>> updateNickname(String newNickname) {
    return ResponseEntity.ok(ApiResponse.success(userService.updateNickname(newNickname)));
  }

  @Override
  public ResponseEntity<ApiResponse<Void>> deleteUser() {
    userService.deleteUser();
    return ResponseEntity.ok(ApiResponse.success());
  }
}
