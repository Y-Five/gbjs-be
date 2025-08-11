/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.user.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.yfive.gbjs.domain.tts.entity.TtsSetting;
import com.yfive.gbjs.domain.user.dto.response.UserDetailResponse;
import com.yfive.gbjs.domain.user.entity.User;

public interface UserService {

  Boolean checkNicknameAvailability(String nickname);

  List<UserDetailResponse> getAllUsers();

  UserDetailResponse getUserDetail();

  String getUserNickname();

  String updateNickname(String newNickname);

  String updateProfileImage(MultipartFile profileImage);

  String updateTtsSetting(TtsSetting ttsSetting);

  boolean toggleEmailMarketingConsent();

  boolean togglePushNotificationConsent();

  boolean toggleLocationConsent();

  void deleteUser();

  User getCurrentUser();
}
