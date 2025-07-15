/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.yfive.gbjs.global.config.MockRedisConfig;
import com.yfive.gbjs.global.config.TestJwtPropertiesConfig;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@Import({TestJwtPropertiesConfig.class, MockRedisConfig.class})
class GbjsApplicationTests {

  @Test
  void contextLoads() {}
}
