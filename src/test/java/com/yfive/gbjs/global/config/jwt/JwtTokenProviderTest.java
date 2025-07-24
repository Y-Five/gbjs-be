/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.config.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.yfive.gbjs.domain.auth.dto.response.TokenResponse;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * JWT 토큰 제공자 테스트 클래스
 *
 * <p>JWT 토큰의 생성, 검증, 인증 정보 추출 등의 기능을 테스트합니다.
 *
 * @author YFIVE
 * @since 1.0.0
 */
class JwtTokenProviderTest {

  private JwtTokenProvider jwtTokenProvider;
  private TokenRepository tokenRepository;
  private JwtProperties jwtProperties;

  // Base64로 인코딩된 시크릿 키 대신 일반 문자열 사용
  private static final String TEST_SECRET =
      "your256bitsecretkeyfordevelopmentenvironmentonlyfortests";
  private static final long ACCESS_TOKEN_VALIDITY = 3600; // 1시간
  private static final long REFRESH_TOKEN_VALIDITY = 604800; // 7일

  /**
   * 각 테스트 실행 전 설정
   *
   * <p>테스트용 JwtTokenProvider 인스턴스를 생성하고 필요한 설정을 주입합니다.
   */
  @BeforeEach
  void setUp() {
    tokenRepository = mock(TokenRepository.class);
    jwtProperties = new JwtProperties();

    // JwtProperties 설정
    jwtProperties.setSecret(TEST_SECRET);
    jwtProperties.setAccessTokenValidityInSeconds(ACCESS_TOKEN_VALIDITY);
    jwtProperties.setRefreshTokenValidityInSeconds(REFRESH_TOKEN_VALIDITY);

    jwtTokenProvider = new JwtTokenProvider(jwtProperties, tokenRepository);
    jwtTokenProvider.init();

    // 블랙리스트 확인 시 항상 false 반환하도록 설정
    when(tokenRepository.isBlacklisted(anyString())).thenReturn(false);
  }

  /**
   * 인증 정보로 JWT 토큰 생성 및 검증 테스트
   */
  @Test
  @DisplayName("인증 정보로 JWT 토큰을 생성하고 검증할 수 있다")
  void createAndValidateToken() {
    // given
    String username = "test-user";
    Authentication authentication = createAuthentication(username);

    // when
    String token = jwtTokenProvider.createToken(authentication);

    // then
    assertThat(token).isNotBlank();
    assertThat(jwtTokenProvider.validateToken(token)).isTrue();
  }

  /**
   * JWT 토큰에서 인증 정보 추출 테스트
   */
  @Test
  @DisplayName("JWT 토큰에서 인증 정보를 추출할 수 있다")
  void getAuthenticationFromToken() {
    // given
    String username = "test-user";
    Authentication authentication = createAuthentication(username);
    String token = jwtTokenProvider.createToken(authentication);

    // when
    Authentication extractedAuth = jwtTokenProvider.getAuthentication(token);

    // then
    assertThat(extractedAuth).isNotNull();
    assertThat(extractedAuth.getName()).isEqualTo(username);
    assertThat(extractedAuth.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactly("ROLE_USER");
  }

  /**
   * 액세스 토큰과 리프레시 토큰 생성 테스트
   */
  @Test
  @DisplayName("액세스 토큰과 리프레시 토큰을 모두 생성할 수 있다")
  void createBothTokens() {
    // given
    String username = "test-user";
    Authentication authentication = createAuthentication(username);

    // when
    TokenResponse tokenResponse = jwtTokenProvider.createTokens(authentication);

    // then
    assertThat(tokenResponse).isNotNull();
    assertThat(tokenResponse.getAccessToken()).isNotBlank();
    assertThat(tokenResponse.getRefreshToken()).isNotBlank();
    assertThat(tokenResponse.getUsername()).isEqualTo(username);
  }

  /**
   * 테스트용 인증 객체 생성
   *
   * @param username 사용자 이름
   * @return 인증 객체
   */
  private Authentication createAuthentication(String email) {
    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");

    Map<String, Object> kakaoAccount = Map.of("email", email);
    Map<String, Object> attributes = Map.of(
        "email", email,
        "kakao_account", kakaoAccount
    );

    OAuth2User principal = new DefaultOAuth2User(
        Collections.singleton(authority),
        attributes,
        "email"
    );

    return new UsernamePasswordAuthenticationToken(principal, "", Collections.singleton(authority));
  }
}
