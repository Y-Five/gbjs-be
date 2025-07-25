/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.user.service;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yfive.gbjs.domain.user.entity.User;
import com.yfive.gbjs.domain.user.exception.UserErrorStatus;
import com.yfive.gbjs.domain.user.repository.UserRepository;
import com.yfive.gbjs.global.error.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Transactional
  @Override
  public boolean toggleEmailMarketingConsent() {
    User user = getCurrentUser();
    user.toggleEmailMarketingConsent();
    userRepository.save(user);
    log.info("사용자 {} 이메일 마케팅 수신 동의 상태 변경: {}", user.getUsername(), user.getEmailMarketingConsent());
    return user.getEmailMarketingConsent();
  }

  @Transactional
  @Override
  public boolean togglePushNotificationConsent() {
    User user = getCurrentUser();
    user.togglePushNotificationConsent();
    userRepository.save(user);
    log.info("사용자 {} 푸시 알림 수신 동의 상태 변경: {}", user.getUsername(), user.getPushNotificationConsent());
    return user.getPushNotificationConsent();
  }

  @Transactional
  @Override
  public boolean toggleLocationConsent() {
    User user = getCurrentUser();
    user.toggleLocationConsent();
    userRepository.save(user);
    log.info("사용자 {} 위치 정보 제공 동의 상태 변경: {}", user.getUsername(), user.getLocationConsent());
    return user.getLocationConsent();
  }

  @Transactional
  @Override
  public String updateNickname(String newNickname) {
    User user = getCurrentUser();

    if (userRepository.findByNickname(newNickname).isPresent()) {
      log.error("닉네임 중복 시도: {}", newNickname);
      throw new CustomException(UserErrorStatus.EXIST_NICKNAME);
    }

    user.updateNickname(newNickname);
    userRepository.save(user);
    log.info("사용자 {} 닉네임 변경: {}", user.getUsername(), newNickname);
    return newNickname;
  }

  @Transactional
  @Override
  public void deleteUser() {
    User user = getCurrentUser();
    userRepository.delete(user);
    log.info("사용자 {}님의 계정이 삭제되었습니다.", user.getUsername());
  }

  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new CustomException(UserErrorStatus.UNAUTHORIZED);
    }

    Object principal = authentication.getPrincipal();
    String username = "";

    if (principal instanceof OAuth2User oauthUser) {
      Map<String, Object> kakaoAccount = oauthUser.getAttribute("kakao_account");
      if (kakaoAccount != null && kakaoAccount.containsKey("email")) {
        username = (String) kakaoAccount.get("email");
      }
    } else if (principal instanceof String str) {
      username = str;
    } else if (principal instanceof UserDetails userDetails) {
      username = userDetails.getUsername();
    } else {
      throw new CustomException(UserErrorStatus.UNAUTHORIZED);
    }

    if (username == null || username.isBlank()) {
      throw new CustomException(UserErrorStatus.UNAUTHORIZED);
    }

    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new CustomException(UserErrorStatus.USER_NOT_FOUND));
  }
}
