/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.kbjs.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.yfive.kbjs.global.config.jwt.JwtProperties;

/**
 * 테스트 환경에서 사용할 JwtProperties 설정
 *
 * <p>테스트 환경에서 JwtProperties를 사용할 수 있도록 설정합니다.
 *
 * @author YFIVE
 * @since 1.0.0
 */
@TestConfiguration
@EnableConfigurationProperties(JwtProperties.class)
public class TestJwtPropertiesConfig {

  /**
   * 테스트용 JwtProperties 빈
   *
   * @return JwtProperties 객체
   */
  @Bean
  public JwtProperties jwtProperties() {
    JwtProperties properties = new JwtProperties();
    properties.setSecret("your256bitsecretkeyfordevelopmentenvironmentonlyfortests");
    properties.setAccessTokenValidityInSeconds(3600);
    properties.setRefreshTokenValidityInSeconds(604800);
    properties.setRefreshTokenTtlInDays(7);
    return properties;
  }
}
