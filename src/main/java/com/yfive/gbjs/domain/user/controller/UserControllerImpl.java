/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.user.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yfive.gbjs.domain.user.dto.response.UserDetailResponse;
import com.yfive.gbjs.domain.user.service.UserService;
import com.yfive.gbjs.global.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserControllerImpl implements UserController {

  private final UserService userService;

  @Override
  public ResponseEntity<ApiResponse<Boolean>> checkNicknameAvailability(
      @RequestParam String nickname) {
    return ResponseEntity.ok(ApiResponse.success(userService.checkNicknameAvailability(nickname)));
  }

  @Override
  public ResponseEntity<ApiResponse<List<UserDetailResponse>>> getAllUsers() {

    return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
  }

  @Override
  public ResponseEntity<ApiResponse<UserDetailResponse>> getUserDetail() {
    return ResponseEntity.ok(ApiResponse.success(userService.getUserDetail()));
  }

  @Override
  public ResponseEntity<ApiResponse<String>> getUserNickname() {
    return ResponseEntity.ok(ApiResponse.success(userService.getUserNickname()));
  }

  @Override
  public ResponseEntity<ApiResponse<String>> updateNickname(
      @RequestParam("newNickname") String newNickname) {
    return ResponseEntity.ok(ApiResponse.success(userService.updateNickname(newNickname)));
  }

  @Override
  public ResponseEntity<ApiResponse<String>> updateProfileImage(
      @RequestPart("profileImage") MultipartFile profileImage) {
    return ResponseEntity.ok(ApiResponse.success(userService.updateProfileImage(profileImage)));
  }

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
  public ResponseEntity<ApiResponse<Void>> deleteUser() {
    userService.deleteUser();
    return ResponseEntity.ok(ApiResponse.success());
  }
}
