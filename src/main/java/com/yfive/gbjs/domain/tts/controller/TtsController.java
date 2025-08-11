/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tts.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yfive.gbjs.domain.tts.dto.request.TtsRequest;
import com.yfive.gbjs.domain.tts.entity.TtsSetting;
import com.yfive.gbjs.global.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "텍스트 음성 변환", description = "TTS 관련 API")
@RequestMapping("/api/tts")
public interface TtsController {

  @PostMapping
  @Operation(summary = "음성 가이드 생성", description = "스크립트를 음성 타입에 맞게 변환된 파일 URL로 변환")
  ResponseEntity<ApiResponse<String>> convertTextToSpeech(
      @Parameter(description = "가이드 식별자", example = "1") @RequestParam Long guideId,
      @Parameter(description = "음성 타입", example = "FEMALE_A") @RequestParam TtsSetting ttsSetting,
      @Parameter(description = "변환할 스크립트", example = "script") @RequestBody @Valid
          TtsRequest request);

  @GetMapping
  @Operation(summary = "음성 가이드 조회", description = "음성 변환된 파일 URL 조회")
  ResponseEntity<ApiResponse<String>> getTextToSpeech(
      @Parameter(description = "가이드 식별자", example = "1") @RequestParam Long guideId);
}
