/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.auth.service;

import com.yfive.gbjs.domain.auth.dto.request.LoginRequest;
import com.yfive.gbjs.domain.auth.dto.response.TokenResponse;

/**
 * 인증 서비스 인터페이스
 *
 * <p>사용자 인증과 관련된 주요 기능들을 제공합니다.
 *
 * <p>주요 기능:
 *
 * <ul>
 *   <li>사용자 로그인
 *   <li>Access Token 재발급
 *   <li>사용자 로그아웃
 *   <li>테스트용 로그인
 * </ul>
 *
 * @author YFIVE
 * @since 1.0.0
 */
public interface AuthService {

  /**
   * 사용자의 아이디와 비밀번호를 검증하여 JWT Access Token과 Refresh Token을 발급합니다.
   *
   * @param loginRequest 로그인 요청 정보(아이디, 비밀번호 포함)
   * @return 발급된 Access Token과 Refresh Token 정보를 담은 {@link TokenResponse} 객체
   */
  TokenResponse login(LoginRequest loginRequest);

  /**
   * Access Token을 기반으로 사용자를 로그아웃 처리합니다.
   *
   * <p>로그아웃 시 해당 사용자의 Refresh Token을 삭제하고, Access Token을 블랙리스트에 등록하여 재사용을 방지합니다.
   *
   * @param accessToken 로그아웃 대상 사용자의 Access Token 문자열
   * @return 로그아웃 성공 메시지
   */
  String logout(String accessToken);

  /**
   * Refresh Token을 사용하여 새로운 Access Token을 발급합니다.
   *
   * <p>기존 Access Token이 만료된 경우 Refresh Token을 검증하고, 유효하면 새로운 Access Token을 발급합니다.
   *
   * @param refreshToken 재발급에 사용할 유효한 Refresh Token 문자열
   * @return 새로 발급된 Access Token 문자열
   */
  String reissueAccessToken(String refreshToken);

  /**
   * 테스트용 로그인 메서드입니다.
   *
   * <p>개발 환경에서 간편하게 로그인 동작을 확인할 때 사용합니다.
   *
   * @return 테스트용 Access Token과 Refresh Token 정보를 담은 {@link TokenResponse} 객체
   */
  TokenResponse testLogin();
}
