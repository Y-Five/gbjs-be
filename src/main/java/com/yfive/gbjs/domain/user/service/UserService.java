/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.user.service;

public interface UserService {

  boolean toggleEmailMarketingConsent();

  boolean togglePushNotificationConsent();

  boolean toggleLocationConsent();

  String updateNickname(String newNickname);

  void deleteUser();
}
