/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.config.jwt;

import com.yfive.gbjs.domain.auth.dto.response.TokenResponse;
import com.yfive.gbjs.domain.user.exception.UserErrorStatus;
import com.yfive.gbjs.global.error.exception.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰 생성 및 검증을 담당하는 클래스
 *
 * <p>이 클래스는 JWT 토큰의 생성, 검증, 파싱 등의 기능을 제공합니다. Access Token과 Refresh Token을 모두 지원하며, Redis를 통한 토큰 관리
 * 기능을 포함합니다.
 *
 * @author YFIVE
 * @since 1.0.0
 */
@Slf4j
@Component
public class JwtTokenProvider {

  /**
   * JWT 설정 속성
   */
  private final JwtProperties jwtProperties;

  /**
   * JWT 서명 키
   */
  private SecretKey key;

  /**
   * 토큰 저장소
   */
  private final TokenRepository tokenRepository;

  /**
   * Access Token 타입 상수
   */
  public static final String TOKEN_TYPE_ACCESS = "access";

  /**
   * Refresh Token 타입 상수
   */
  public static final String TOKEN_TYPE_REFRESH = "refresh";

  /**
   * 생성자
   *
   * @param jwtProperties   JWT 설정 속성
   * @param tokenRepository 토큰 저장소
   */
  public JwtTokenProvider(JwtProperties jwtProperties, TokenRepository tokenRepository) {
    this.jwtProperties = jwtProperties;
    this.tokenRepository = tokenRepository;
  }

  /**
   * 초기화 메서드
   *
   * <p>시크릿 키를 Base64로 디코딩하여 JWT 서명 키를 생성합니다. 디코딩에 실패할 경우 일반 텍스트로 처리합니다.
   */
  @PostConstruct
  public void init() {
    // Base64 디코딩 시도
    try {
      byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
      this.key = Keys.hmacShaKeyFor(keyBytes);
    } catch (Exception e) {
      // 디코딩 실패 시 일반 텍스트로 처리
      log.warn("Base64 디코딩 실패, 일반 텍스트로 처리합니다: {}", e.getMessage());
      this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }
    log.info("JWT key initialized");
  }

  /**
   * Access Token과 Refresh Token을 모두 생성
   *
   * <p>인증 정보를 기반으로 Access Token과 Refresh Token을 생성하고, Refresh Token은 Redis에 저장합니다.
   *
   * @param authentication 인증 정보
   * @return 토큰 응답 객체
   */
  public TokenResponse createTokens(Authentication authentication) {
    String username = authentication.getName();

    // Access Token 생성
    String accessToken = createToken(authentication, TOKEN_TYPE_ACCESS);

    // Refresh Token 생성
    String refreshToken = createToken(authentication, TOKEN_TYPE_REFRESH);

    // Refresh Token을 Redis에 저장
    tokenRepository.saveRefreshToken(username, refreshToken);

    // TokenResponse 객체 생성하여 반환
    return TokenResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .username(username)
        .build();
  }

  /**
   * 지정된 타입의 JWT 토큰 생성
   *
   * <p>인증 정보와 토큰 타입을 기반으로 JWT 토큰을 생성합니다. 토큰 타입에 따라 유효 기간이 다르게 설정됩니다.
   *
   * @param authentication 인증 정보
   * @param tokenType      토큰 타입 (access 또는 refresh)
   * @return 생성된 JWT 토큰
   */
  public String createToken(Authentication authentication, String tokenType) {
    String authorities =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");

    if (kakaoAccount == null || !kakaoAccount.containsKey("email")) {
      throw new CustomException(UserErrorStatus.UNAUTHORIZED);
    }

    String email = (String) kakaoAccount.get("email");

    long now = System.currentTimeMillis();
    Date validity;

    // 토큰 타입에 따라 유효기간 설정
    if (TOKEN_TYPE_REFRESH.equals(tokenType)) {
      validity = new Date(now + jwtProperties.getRefreshTokenValidityInSeconds() * 1000);
    } else {
      validity = new Date(now + jwtProperties.getAccessTokenValidityInSeconds() * 1000);
    }

    return Jwts.builder()
        .setSubject(email)
        .claim("auth", authorities)
        .claim("type", tokenType)
        .setIssuedAt(new Date(now))
        .setExpiration(validity)
        .signWith(key)
        .compact();
  }

  /**
   * Access Token 생성 (하위 호환성 유지)
   *
   * @param authentication 인증 정보
   * @return 생성된 Access Token
   */
  public String createToken(Authentication authentication) {
    return createToken(authentication, TOKEN_TYPE_ACCESS);
  }

  /**
   * 토큰에서 인증 정보 추출
   *
   * <p>JWT 토큰을 파싱하여 사용자 인증 정보를 추출합니다.
   *
   * @param token JWT 토큰
   * @return 인증 정보
   */
  public Authentication getAuthentication(String token) {
    Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

    Collection<? extends GrantedAuthority> authorities =
        Arrays.stream(claims.get("auth").toString().split(","))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

    Map<String, Object> attributes = Map.of("email", claims.getSubject());
    OAuth2User principal = new DefaultOAuth2User(authorities, attributes, "email");
    return new UsernamePasswordAuthenticationToken(principal, token, authorities);
  }

  /**
   * 토큰에서 사용자 이름 추출
   *
   * @param token JWT 토큰
   * @return 사용자 이름
   */
  public String getUsernameFromToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  /**
   * 토큰에서 토큰 타입 추출
   *
   * @param token JWT 토큰
   * @return 토큰 타입 (access 또는 refresh)
   */
  public String getTokenType(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .get("type", String.class);
  }

  /**
   * 토큰 유효성 검사
   *
   * <p>JWT 토큰의 유효성을 검사합니다. 블랙리스트에 등록된 토큰은 유효하지 않습니다.
   *
   * @param token JWT 토큰
   * @return 유효성 여부
   */
  public boolean validateToken(String token) {
    try {
      // 블랙리스트 확인
      if (tokenRepository.isBlacklisted(token)) {
        log.info("블랙리스트에 등록된 토큰입니다.");
        return false;
      }

      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (SignatureException | MalformedJwtException e) {
      log.info("잘못된 JWT 서명입니다.");
      log.trace("잘못된 JWT 서명 추적: {}", e);
    } catch (ExpiredJwtException e) {
      log.info("만료된 JWT 토큰입니다.");
      log.trace("만료된 JWT 토큰 추적: {}", e);
    } catch (UnsupportedJwtException e) {
      log.info("지원되지 않는 JWT 토큰입니다.");
      log.trace("지원되지 않는 JWT 토큰 추적: {}", e);
    } catch (IllegalArgumentException e) {
      log.info("JWT 토큰이 잘못되었습니다.");
      log.trace("JWT 토큰이 잘못된 추적: {}", e);
    }
    return false;
  }

  /**
   * 특정 토큰 타입 검증
   *
   * <p>토큰의 타입이 기대하는 타입과 일치하는지 검증합니다.
   *
   * @param token        JWT 토큰
   * @param expectedType 기대하는 토큰 타입
   * @return 일치 여부
   */
  public boolean validateTokenType(String token, String expectedType) {
    try {
      String tokenType = getTokenType(token);
      return expectedType.equals(tokenType);
    } catch (Exception e) {
      log.info("토큰 타입 검증 실패: {}", e.getMessage());
      return false;
    }
  }

  /**
   * 토큰 만료 시간 추출
   *
   * <p>토큰의 남은 유효 시간을 초 단위로 반환합니다.
   *
   * @param token JWT 토큰
   * @return 남은 유효 시간 (초)
   */
  public long getExpirationTime(String token) {
    try {
      Date expiration =
          Jwts.parserBuilder()
              .setSigningKey(key)
              .build()
              .parseClaimsJws(token)
              .getBody()
              .getExpiration();
      return (expiration.getTime() - System.currentTimeMillis()) / 1000;
    } catch (Exception e) {
      return 0;
    }
  }

  /**
   * 토큰 블랙리스트에 추가
   *
   * <p>로그아웃 시 토큰을 블랙리스트에 추가하여 더 이상 사용할 수 없게 합니다.
   *
   * @param token JWT 토큰
   */
  public void blacklistToken(String token) {
    long expiration = getExpirationTime(token);
    if (expiration > 0) {
      tokenRepository.addToBlacklist(token, expiration);
    }
  }

  /**
   * Refresh Token 검증
   *
   * <p>사용자의 Refresh Token이 Redis에 저장된 토큰과 일치하는지 검증합니다.
   *
   * @param username     사용자 이름
   * @param refreshToken Refresh Token
   * @return 일치 여부
   */
  public boolean validateRefreshToken(String username, String refreshToken) {
    String storedToken = tokenRepository.findRefreshToken(username);
    return storedToken != null && storedToken.equals(refreshToken);
  }

  /**
   * JWT 토큰을 HTTP 응답의 쿠키에 추가합니다.
   *
   * <p>이 메서드는 주어진 JWT 토큰을 HttpOnly 및 Secure 속성이 설정된 쿠키로 만들어 응답에 추가합니다. 이 쿠키는 클라이언트에서 JavaScript로
   * 접근할 수 없으며, HTTPS 환경에서만 전송됩니다.
   *
   * @param response 응답 객체 (HttpServletResponse)
   * @param token    쿠키에 저장할 JWT 토큰 값
   * @param name     쿠키 이름 (예: "accessToken", "refreshToken")
   * @param maxAge   쿠키의 유효 시간 (밀리초 단위)
   */
  public void addJwtToCookie(HttpServletResponse response, String token, String name, long maxAge) {
    Cookie cookie = new Cookie(name, token);
    cookie.setHttpOnly(true);
    // cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge((int) maxAge);
    response.addCookie(cookie);

    log.info("JWT 쿠키가 설정되었습니다 - 이름: {}, 만료: {}초", name, cookie.getMaxAge());
  }
}
