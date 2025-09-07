/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.chat.controller;

import com.yfive.gbjs.domain.chat.dto.request.ChatRequest;
import com.yfive.gbjs.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "챗봇", description = "챗봇 관련 API (OpenAI)")
@RequestMapping("/api/chat")
public interface ChatController {

  @PostMapping
  @Operation(summary = "챗봇 질문 요청", description = "챗봇을 사용해 서비스에 대한 질문을 요청합니다.")
  ResponseEntity<ApiResponse<String>> chat(@Valid @RequestBody ChatRequest request);
}
