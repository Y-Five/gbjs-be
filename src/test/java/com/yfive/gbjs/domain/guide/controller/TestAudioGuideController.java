/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yfive.gbjs.global.common.response.ApiResponse;

/** 테스트용 오디오 가이드 컨트롤러 */
@RestController
@RequestMapping("/api/audio-guide/test")
public class TestAudioGuideController {

  @GetMapping("/count")
  public ResponseEntity<ApiResponse<String>> getMockAudioGuideCount() {
    return ResponseEntity.ok(ApiResponse.success("테스트 AudioGuide 카운트: 100"));
  }
}
