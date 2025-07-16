/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.user.dto.response;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 보호된 리소스 응답 DTO
 *
 * <p>인증된 사용자가 접근할 수 있는 보호된 리소스 정보를 담는 DTO입니다.
 *
 * @author YFIVE
 * @since 1.0.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "보호된 리소스 응답")
public class ProtectedResourceResponse {

  @Schema(description = "메시지", example = "인증된 사용자만 볼 수 있는 정보입니다.")
  private String message;

  @Schema(description = "사용자 이름", example = "홍길동")
  private String username;

  @Schema(description = "사용자 권한 목록", example = "ROLE_USER")
  private Collection<? extends GrantedAuthority> authorities;
}
