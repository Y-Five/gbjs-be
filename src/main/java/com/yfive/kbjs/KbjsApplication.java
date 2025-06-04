/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.kbjs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class KbjsApplication {

  public static void main(String[] args) {
    SpringApplication.run(KbjsApplication.class, args);
  }
}
