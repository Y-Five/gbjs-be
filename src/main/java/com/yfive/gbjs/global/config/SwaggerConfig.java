/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Swagger(OpenAPI) 설정 클래스
 *
 * <p>API 문서화를 위한 Swagger/OpenAPI 설정을 제공합니다.
 *
 * @author YFIVE
 * @since 1.0.0
 */
@Configuration
public class SwaggerConfig {

  private static final String SECURITY_SCHEME_NAME = "bearerAuth";

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .addServersItem(new Server().url("/").description("Default Server URL"))
        .info(
            new Info()
                .title("GBJS API")
                .description("GBJS 프로젝트 API 문서")
                .version("v1.0.0")
                .contact(new Contact().name("YFIVE").email("contact@yfive.com")))
        .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
        .components(
            new Components()
                .addSecuritySchemes(
                    SECURITY_SCHEME_NAME,
                    new SecurityScheme()
                        .name(SECURITY_SCHEME_NAME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
  }
}
