/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * Mock Redis 설정 클래스
 *
 * <p>테스트 환경에서 사용할 Mock Redis 설정을 제공합니다. 실제 Redis 서버 없이 테스트를 실행할 수 있도록 합니다.
 *
 * @author YFIVE
 * @since 1.0.0
 */
@TestConfiguration
@Profile({"test", "ci"})
public class MockRedisConfig {

  private final Map<String, Object> redisStorage = new HashMap<>();

  /**
   * Mock Redis 연결 팩토리
   *
   * @return Mock Redis 연결 팩토리
   */
  @Bean
  @Primary
  public RedisConnectionFactory redisConnectionFactory() {
    RedisConnectionFactory factory = Mockito.mock(RedisConnectionFactory.class);
    RedisConnection connection = Mockito.mock(RedisConnection.class);
    when(factory.getConnection()).thenReturn(connection);
    return factory;
  }

  /**
   * Mock Redis 템플릿
   *
   * @return Mock Redis 템플릿
   */
  @Bean
  @Primary
  @SuppressWarnings("unchecked")
  public RedisTemplate<String, Object> redisTemplate() {
    RedisTemplate<String, Object> redisTemplate = Mockito.mock(RedisTemplate.class);
    ValueOperations<String, Object> valueOperations = Mockito.mock(ValueOperations.class);

    // ValueOperations 모킹
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    // set 메서드 모킹
    doNothing().when(valueOperations).set(anyString(), any());
    doNothing().when(valueOperations).set(anyString(), any(), anyLong(), any(TimeUnit.class));

    // get 메서드 모킹
    when(valueOperations.get(anyString()))
        .thenAnswer(
            invocation -> {
              String key = invocation.getArgument(0);
              return redisStorage.get(key);
            });

    // hasKey 메서드 모킹
    when(redisTemplate.hasKey(anyString()))
        .thenAnswer(
            invocation -> {
              String key = invocation.getArgument(0);
              return redisStorage.containsKey(key);
            });

    // delete 메서드 모킹
    when(redisTemplate.delete(anyString()))
        .thenAnswer(
            invocation -> {
              String key = invocation.getArgument(0);
              return redisStorage.remove(key) != null;
            });

    return redisTemplate;
  }
}
