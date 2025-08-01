/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.security;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.yfive.gbjs.domain.user.entity.User;
import com.yfive.gbjs.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final UserRepository userRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
    OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(request);
    String registrationId = request.getClientRegistration().getRegistrationId();

    Map<String, Object> attributes = oauth2User.getAttributes();

    String email = extractEmail(registrationId, attributes);

    String nickname = extractNickname(registrationId, attributes);

    User user =
        userRepository
            .findByUsername(email)
            .orElseGet(() -> userRepository.save(User.fromOAuth(email, nickname)));

    log.info("사용자 로그인 성공: {}", user.getUsername());

    String nameAttributeKey = "id"; // Kakao의 경우 attributes에 반드시 존재하는 키
    Object nameAttr = attributes.get(nameAttributeKey);
    if (nameAttr == null) {
      throw new OAuth2AuthenticationException("Missing required attribute: " + nameAttributeKey);
    }

    return new DefaultOAuth2User(
        Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
        attributes,
        nameAttributeKey);
  }

  private String extractEmail(String provider, Map<String, Object> attributes) {
    if ("kakao".equals(provider)) {
      Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
      if (account == null || !account.containsKey("email")) {
        log.warn("카카오 계정에서 이메일을 찾을 수 없습니다.");
        throw new OAuth2AuthenticationException("카카오 계정에 이메일이 없습니다.");
      }
      return (String) account.get("email");
    }
    throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다.");
  }

  private String extractNickname(String provider, Map<String, Object> attributes) {
    String nickname = null;
    if ("kakao".equals(provider)) {
      Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
      Map<String, Object> profile =
          account != null ? (Map<String, Object>) account.get("profile") : null;
      if (profile != null && profile.get("nickname") != null) {
        nickname = (String) profile.get("nickname");
      }
    }
    if (nickname == null) {
      nickname = "user_" + UUID.randomUUID().toString().substring(0, 8);
      log.info("닉네임 정보 없음 - 랜덤 닉네임 생성: {}", nickname);
    }
    return nickname;
  }
}
