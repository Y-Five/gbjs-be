/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "profile_image_url", nullable = false)
  private String profileImageUrl;

  @Column(name = "nickname", nullable = false, unique = true)
  private String nickname;

  @Column(name = "username", nullable = false, unique = true)
  private String username;

  @Column(name = "email_marketing_consent", nullable = false)
  private Boolean emailMarketingConsent;

  // 알림 수신 여부 (푸시 알림)
  @Column(name = "push_notification_consent", nullable = false)
  private Boolean pushNotificationConsent;

  // 위치 정보 수집 동의 여부
  @Column(name = "location_consent", nullable = false)
  private Boolean locationConsent;

  public static User fromOAuth(String email, String profileImageUrl, String nickname) {
    return User.builder()
        .username(email)
        .nickname(nickname)
        .profileImageUrl(profileImageUrl)
        .emailMarketingConsent(false)
        .pushNotificationConsent(false)
        .locationConsent(false)
        .build();
  }

  public void updateNickname(String newNickname) {
    this.nickname = newNickname;
  }

  public void updateProfileImageUrl(String profileImageUrl) {
    this.profileImageUrl = profileImageUrl;
  }

  public void toggleEmailMarketingConsent() {
    this.emailMarketingConsent = this.emailMarketingConsent == null || !this.emailMarketingConsent;
  }

  public void togglePushNotificationConsent() {
    this.pushNotificationConsent =
        this.pushNotificationConsent == null || !this.pushNotificationConsent;
  }

  public void toggleLocationConsent() {
    this.locationConsent = this.locationConsent == null || !this.locationConsent;
  }
}
