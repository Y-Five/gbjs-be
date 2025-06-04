/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.kbjs.global.config.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * JWT 관련 설정 속성
 *
 * <p>application.properties 또는 application.yml에서 jwt 접두사를 가진 속성들을 관리합니다.
 *
 * @author YFIVE
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

  /** JWT 시크릿 키 */
  private String secret = "your-256-bit-secret-key-for-development-environment-only";

  /** Access Token 유효 기간 (초) */
  private long accessTokenValidityInSeconds = 3600;

  /** Refresh Token 유효 기간 (초) */
  private long refreshTokenValidityInSeconds = 604800;

  /** Refresh Token 유효 기간 (일) */
  private int refreshTokenTtlInDays = 7;
}
