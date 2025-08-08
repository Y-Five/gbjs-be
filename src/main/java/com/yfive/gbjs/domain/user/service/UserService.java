/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.user.service;

import com.yfive.gbjs.domain.user.entity.User;

public interface UserService {

  boolean toggleEmailMarketingConsent();

  boolean togglePushNotificationConsent();

  boolean toggleLocationConsent();

  String updateNickname(String newNickname);

  void deleteUser();

  User getCurrentUser();
}
