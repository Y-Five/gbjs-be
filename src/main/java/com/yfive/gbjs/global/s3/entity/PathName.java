/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.s3.entity;

import io.swagger.v3.oas.annotations.media.Schema;

public enum PathName {
  @Schema(description = "프로필사진")
  PROFILE_IMAGE,
  @Schema(description = "띠부씰")
  SEAL,
  @Schema(description = "특산품")
  SPECIALTIES,
  @Schema(description = "문화체험")
  ACTIVITY
}
