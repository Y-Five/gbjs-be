/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.user.dto.response;

import com.yfive.gbjs.domain.tts.entity.TtsSetting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "UserDetailResponse DTO", description = "사용자 정보 응답 반환")
public class UserDetailResponse {

  @Schema(description = "사용자 식별자", example = "1")
  private Long userId;

  @Schema(
      description = "프로필 이미지 URL",
      example = "http://k.kakaocdn.net/dn/oOPCG/btsPjlOHjk6/6jx0PyBKkHHyCfbV8IY741/img_640x640.jpg")
  private String profileImageUrl;

  @Schema(description = "닉네임", example = "나나나난")
  private String nickname;

  @Schema(description = "띠부씰 수집 개수", example = "17")
  private Long sealCount;

  @Schema(description = "아이디(이메일)", example = "heritage@example.com")
  private String username;

  @Schema(description = "음성 타입", example = "FEMALE_A")
  private TtsSetting ttsSetting;

  @Schema(description = "이메일 수신 동의 여부", example = "true")
  private Boolean emailMarketingConsent;

  @Schema(description = "알림 수신 동의 여부", example = "false")
  private Boolean pushNotificationConsent;

  @Schema(description = "위치 정보 수집 동의 여부", example = "true")
  private Boolean locationConsent;
}
