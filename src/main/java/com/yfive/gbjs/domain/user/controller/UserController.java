/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.user.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.yfive.gbjs.domain.user.dto.response.UserDetailResponse;
import com.yfive.gbjs.global.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "사용자", description = "사용자 관련 API")
@RequestMapping("/api/users")
public interface UserController {

  @GetMapping("/nickname/check")
  @Operation(
      summary = "닉네임 중복 여부 확인",
      description =
          """
              사용자가 입력한 닉네임이 이미 존재하는지 여부를 반환합니다.
              true -> 이미 존재하는 닉네임, 변경할 수 없음.
              false -> 존재하지 않는 닉네임, 변경 가능.
              """)
  ResponseEntity<ApiResponse<Boolean>> checkNicknameDuplicated(@RequestParam String nickname);

  @GetMapping("/dev")
  @Operation(summary = "[개발자]사용자 전체 조회", description = "스웨거를 사용해 전체 사용자를 조회합니다.")
  ResponseEntity<ApiResponse<List<UserDetailResponse>>> getAllUsers();

  @GetMapping
  @Operation(summary = "사용자 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
  ResponseEntity<ApiResponse<UserDetailResponse>> getUserDetail();

  @GetMapping("/nickname")
  @Operation(summary = "사용자 닉네임 조회", description = "현재 로그인한 사용자의 닉네임을 조회합니다.")
  ResponseEntity<ApiResponse<String>> getUserNickname();

  @PutMapping("/nickname")
  @Operation(summary = "닉네임 수정", description = "현재 로그인한 사용자의 닉네임을 수정합니다.")
  ResponseEntity<ApiResponse<String>> updateNickname(
      @RequestParam("newNickname") String newNickname);

  @PutMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "프로필 사진 수정", description = "현재 로그인한 사용자의 프로필 사진을 수정합니다.")
  ResponseEntity<ApiResponse<String>> updateProfileImage(
      @RequestPart("profileImage") MultipartFile profileImage);

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

  @DeleteMapping
  @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 계정을 삭제합니다. (Hard Delete)")
  ResponseEntity<ApiResponse<Void>> deleteUser();
}
