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
    log.info(
        "사용자 {} 이메일 마케팅 수신 동의 상태 변경: {}",
        maskEmail(user.getUsername()),
        user.getEmailMarketingConsent());
    return user.getEmailMarketingConsent();
  }

  @Transactional
  @Override
  public boolean togglePushNotificationConsent() {
    User user = getCurrentUser();
    user.togglePushNotificationConsent();
    userRepository.save(user);
    log.info(
        "사용자 {} 푸시 알림 수신 동의 상태 변경: {}",
        maskEmail(user.getUsername()),
        user.getPushNotificationConsent());
    return user.getPushNotificationConsent();
  }

  @Transactional
  @Override
  public boolean toggleLocationConsent() {
    User user = getCurrentUser();
    user.toggleLocationConsent();
    userRepository.save(user);
    log.info(
        "사용자 {} 위치 정보 제공 동의 상태 변경: {}", maskEmail(user.getUsername()), user.getLocationConsent());
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
    log.info("사용자 {} 닉네임 변경: {}", maskEmail(user.getUsername()), newNickname);
    return newNickname;
  }

  @Transactional
  @Override
  public void deleteUser() {
    User user = getCurrentUser();
    userRepository.delete(user);
    log.info("사용자 {}님의 계정이 삭제되었습니다.", maskEmail(user.getUsername()));
  }

  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new CustomException(UserErrorStatus.UNAUTHORIZED);
    }

    Object principal = authentication.getPrincipal();
    String username = "";

    if (principal instanceof OAuth2User oauthUser) {
      // JWT에서 생성된 OAuth2User는 email 속성을 직접 가지고 있음
      Object email = oauthUser.getAttribute("email");
      if (email != null) {
        username = (String) email;
      } else {
        // 카카오 OAuth2 로그인의 경우
        Map<String, Object> kakaoAccount = oauthUser.getAttribute("kakao_account");
        if (kakaoAccount != null && kakaoAccount.containsKey("email")) {
          username = (String) kakaoAccount.get("email");
        }
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

    log.debug("JWT에서 추출한 email: {}", maskEmail(username));

    final String finalUsername = username; // final 변수로 복사
    return userRepository
        .findByUsername(finalUsername)
        .orElseThrow(
            () -> {
              log.error("사용자를 찾을 수 없음: {}", maskEmail(finalUsername));
              return new CustomException(UserErrorStatus.USER_NOT_FOUND);
            });
  }

  /**
   * 이메일 마스킹 처리
   *
   * @param email 원본 이메일
   * @return 마스킹된 이메일 (예: te***@example.com)
   */
  private String maskEmail(String email) {
    if (email == null || !email.contains("@")) {
      return "***";
    }

    String[] parts = email.split("@");
    String localPart = parts[0];
    String domain = parts[1];

    if (localPart.length() <= 2) {
      return "***@" + domain;
    }

    return localPart.substring(0, 2) + "***@" + domain;
  }
}
