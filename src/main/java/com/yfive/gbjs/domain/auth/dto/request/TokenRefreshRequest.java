/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 토큰 갱신 요청 DTO
 *
 * @param refreshToken 리프레시 토큰
 */
@Schema(description = "토큰 갱신 요청")
public record TokenRefreshRequest(
    @NotBlank(message = "리프레시 토큰은 필수입니다.")
        @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken) {}
