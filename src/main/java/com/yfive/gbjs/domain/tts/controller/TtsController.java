/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tts.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yfive.gbjs.domain.tts.dto.request.TtsRequest;
import com.yfive.gbjs.domain.tts.dto.response.TtsResponse;
import com.yfive.gbjs.domain.tts.entity.Gender;
import com.yfive.gbjs.domain.tts.entity.SpeechType;
import com.yfive.gbjs.global.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "텍스트 음성 변환", description = "TTS 관련 API")
@RequestMapping("/api/tts")
public interface TtsController {

  @PostMapping("/convert-to-speech")
  @Operation(summary = "[개발자]음성 가이드 생성", description = "스웨거를 사용해 스크립트를 음성가이드로 변환")
  ResponseEntity<ApiResponse<TtsResponse>> convertTextToSpeech(
      @Parameter(description = "가이드 식별자", example = "1") @RequestParam Long guideId,
      @Parameter(description = "음성 성별", example = "FEMALE") @RequestParam Gender gender,
      @Parameter(description = "음성 타입", example = "A") @RequestParam SpeechType speechType,
      @Parameter(description = "변환할 스크립트", example = "script") @RequestBody TtsRequest request);
}
