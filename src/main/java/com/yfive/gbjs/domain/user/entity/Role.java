/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;

public enum Role {
  @Schema(description = "사용자")
  ROLE_USER,
  @Schema(description = "관리자")
  ROLE_ADMIN,
  @Schema(description = "개발자")
  ROLE_DEVELOPER;
}
