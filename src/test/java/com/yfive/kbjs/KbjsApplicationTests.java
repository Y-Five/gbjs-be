/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.kbjs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.yfive.kbjs.global.config.MockRedisConfig;
import com.yfive.kbjs.global.config.TestJwtPropertiesConfig;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@Import({TestJwtPropertiesConfig.class, MockRedisConfig.class})
class KbjsApplicationTests {

  @Test
  void contextLoads() {}
}
