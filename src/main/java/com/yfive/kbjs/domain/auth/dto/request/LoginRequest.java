/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.kbjs.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 로그인 요청 DTO
 *
 * @param username 사용자 아이디
 * @param password 비밀번호
 */
@Schema(description = "로그인 요청")
public record LoginRequest(
    @NotBlank(message = "사용자 아이디는 필수입니다.") @Schema(description = "사용자 아이디", example = "user")
        String username,
    @NotBlank(message = "비밀번호는 필수입니다.") @Schema(description = "비밀번호", example = "password")
        String password) {}
