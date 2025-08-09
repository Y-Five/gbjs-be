/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.user.service;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.yfive.gbjs.domain.seal.repository.UserSealRepository;
import com.yfive.gbjs.domain.user.dto.response.UserDetailResponse;
import com.yfive.gbjs.domain.user.entity.User;
import com.yfive.gbjs.domain.user.exception.UserErrorStatus;
import com.yfive.gbjs.domain.user.mapper.UserMapper;
import com.yfive.gbjs.domain.user.repository.UserRepository;
import com.yfive.gbjs.global.error.exception.CustomException;
import com.yfive.gbjs.global.s3.entity.PathName;
import com.yfive.gbjs.global.s3.exception.S3ErrorStatus;
import com.yfive.gbjs.global.s3.service.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserSealRepository userSealRepository;
  private final S3Service s3Service;
  private final UserMapper userMapper;

  @Override
  public Boolean checkNicknameAvailability(String nickname) {
    boolean exists = userRepository.findByNickname(nickname).isPresent();
    log.info("닉네임 중복 체크 - nickname: {}, exists: {}", nickname, exists);
    return exists;
  }

  @Override
  public List<UserDetailResponse> getAllUsers() {

    List<User> allUsers = userRepository.findAll();

    List<UserDetailResponse> userDetails =
        allUsers.stream()
            .map(
                user -> {
                  Long sealCount = (long) userSealRepository.findByUserId(user.getId()).size();
                  return userMapper.toUserDetailResponse(user, sealCount);
                })
            .toList();

    log.info("전체 사용자 조회, 총 사용자 수: {}", userDetails.size());

    return userDetails;
  }

  @Override
  public UserDetailResponse getUserDetail() {
    User user = getCurrentUser();
    log.info("사용자 상세 조회 - userId: {}", user.getId());

    Long sealCount = (long) userSealRepository.findByUserId(user.getId()).size();

    return userMapper.toUserDetailResponse(user, sealCount);
  }

  @Override
  public String getUserNickname() {
    String nickname = getCurrentUser().getNickname();
    log.info("사용자 닉네임 조회 - nickname: {}", nickname);
    return nickname;
  }

  @Transactional
  @Override
  public String updateNickname(String newNickname) {
    User user = getCurrentUser();

    if (userRepository.findByNickname(newNickname).isPresent()) {
      log.error("닉네임 중복 시도 - userId: {}, nickname: {}", user.getId(), newNickname);
      throw new CustomException(UserErrorStatus.EXIST_NICKNAME);
    }

    user.updateNickname(newNickname);
    userRepository.save(user);
    log.info("사용자 닉네임 변경 - userId: {}, newNickname: {}", user.getId(), newNickname);
    return newNickname;
  }

  @Transactional
  @Override
  public String updateProfileImage(MultipartFile profileImage) {
    User user = getCurrentUser();

    if (profileImage == null || profileImage.isEmpty()) {
      log.warn("프로필 이미지 변경 요청이 비어있음 - userId: {}", user.getId());
      return user.getProfileImageUrl();
    }

    String newImageUrl = user.getProfileImageUrl();

    try {
      newImageUrl = s3Service.uploadFile(PathName.PROFILE_IMAGE, profileImage);
    } catch (Exception e) {
      log.error("S3 파일 업로드 실패(교체) - userId: {}", user.getId(), e);
      throw new CustomException(S3ErrorStatus.FILE_SERVER_ERROR);
    }

    user.updateProfileImageUrl(newImageUrl);

    log.info("사용자 프로필 이미지 변경 - userId: {}, newImageUrl: {}", user.getId(), newImageUrl);

    return newImageUrl;
  }

  @Transactional
  @Override
  public boolean toggleEmailMarketingConsent() {
    User user = getCurrentUser();
    user.toggleEmailMarketingConsent();
    userRepository.save(user);
    log.info(
        "사용자 이메일 마케팅 수신 동의 상태 변경 - userId: {}, status: {}",
        user.getId(),
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
        "사용자 푸시 알림 수신 동의 상태 변경 - userId: {}, status: {}",
        user.getId(),
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
        "사용자 위치 정보 제공 동의 상태 변경 - userId: {}, status: {}", user.getId(), user.getLocationConsent());
    return user.getLocationConsent();
  }

  @Transactional
  @Override
  public void deleteUser() {
    User user = getCurrentUser();
    userRepository.delete(user);
    log.info("사용자 계정 삭제 - userId: {}", user.getId());
  }

  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      log.error("인증 실패 - 인증 정보 없음");
      throw new CustomException(UserErrorStatus.UNAUTHORIZED);
    }

    Object principal = authentication.getPrincipal();
    String username = "";

    try {
      if (principal instanceof OAuth2User oauthUser) {
        Object email = oauthUser.getAttribute("email");
        if (email != null) {
          username = (String) email;
        } else {
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
        log.error("인증 실패 - Principal 타입 알 수 없음: {}", principal.getClass());
        throw new CustomException(UserErrorStatus.UNAUTHORIZED);
      }
    } catch (Exception e) {
      log.error("인증 정보 추출 중 오류", e);
      throw new CustomException(UserErrorStatus.UNAUTHORIZED);
    }

    if (username == null || username.isBlank()) {
      log.error("인증 실패 - 추출된 username이 null 또는 빈 문자열");
      throw new CustomException(UserErrorStatus.UNAUTHORIZED);
    }

    log.debug("JWT에서 추출한 email: {}", maskEmail(username));

    final String finalUsername = username;
    return userRepository
        .findByUsername(finalUsername)
        .orElseThrow(
            () -> {
              log.error("사용자 찾기 실패 - username: {}", maskEmail(finalUsername));
              return new CustomException(UserErrorStatus.USER_NOT_FOUND);
            });
  }

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
