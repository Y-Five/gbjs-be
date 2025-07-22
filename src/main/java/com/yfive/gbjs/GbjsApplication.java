/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@ConfigurationPropertiesScan
public class GbjsApplication {

  public static void main(String[] args) {
    SpringApplication.run(com.yfive.gbjs.GbjsApplication.class, args);
  }
}
