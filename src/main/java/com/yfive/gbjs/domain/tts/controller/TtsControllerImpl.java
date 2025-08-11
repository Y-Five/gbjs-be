/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tts.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yfive.gbjs.domain.tts.dto.request.TtsRequest;
import com.yfive.gbjs.domain.tts.entity.TtsSetting;
import com.yfive.gbjs.domain.tts.service.TtsService;
import com.yfive.gbjs.global.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TtsControllerImpl implements TtsController {

  private final TtsService ttsService;

  @Override
  public ResponseEntity<ApiResponse<String>> convertTextToSpeech(
      @RequestParam Long guideId,
      @RequestParam TtsSetting ttsSetting,
      @RequestBody TtsRequest request) {

    return ResponseEntity.ok(
        ApiResponse.success(ttsService.convertTextToSpeech(guideId, ttsSetting, request)));
  }

  @Override
  public ResponseEntity<ApiResponse<String>> getTextToSpeech(@RequestParam Long guideId) {

    return ResponseEntity.ok(ApiResponse.success(ttsService.getTextToSpeech(guideId)));
  }
}
