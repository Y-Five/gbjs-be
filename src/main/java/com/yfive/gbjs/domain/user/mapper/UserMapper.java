/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.user.mapper;

import org.springframework.stereotype.Component;

import com.yfive.gbjs.domain.user.dto.response.UserDetailResponse;
import com.yfive.gbjs.domain.user.entity.User;

@Component
public class UserMapper {

  public UserDetailResponse toUserDetailResponse(User user, Long sealCount) {
    return UserDetailResponse.builder()
        .userId(user.getId())
        .profileImageUrl(user.getProfileImageUrl())
        .nickname(user.getNickname())
        .sealCount(sealCount)
        .username(user.getUsername())
        .emailMarketingConsent(user.getEmailMarketingConsent())
        .pushNotificationConsent(user.getPushNotificationConsent())
        .locationConsent(user.getLocationConsent())
        .build();
  }
}
