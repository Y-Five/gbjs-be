/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tts.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yfive.gbjs.domain.tts.dto.request.TtsRequest;
import com.yfive.gbjs.domain.tts.dto.response.TtsResponse;
import com.yfive.gbjs.domain.tts.entity.Gender;
import com.yfive.gbjs.domain.tts.entity.SpeechType;
import com.yfive.gbjs.domain.tts.service.TtsService;
import com.yfive.gbjs.global.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TtsControllerImpl implements TtsController {

  private final TtsService ttsService;

  @Override
  public ResponseEntity<ApiResponse<TtsResponse>> convertTextToSpeech(
      @RequestParam Long guideId,
      @RequestParam Gender gender,
      @RequestParam SpeechType speechType,
      @RequestBody TtsRequest request) {

    TtsResponse response = ttsService.convertTextToSpeech(guideId, gender, speechType, request);

    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
