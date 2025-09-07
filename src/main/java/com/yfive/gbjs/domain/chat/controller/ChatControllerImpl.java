/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.chat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.yfive.gbjs.domain.chat.dto.request.ChatRequest;
import com.yfive.gbjs.domain.chat.service.ChatService;
import com.yfive.gbjs.global.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatControllerImpl implements ChatController {

  private final ChatService chatService;

  @Override
  public ResponseEntity<ApiResponse<String>> chat(@RequestBody ChatRequest request) {

    return ResponseEntity.ok(ApiResponse.success(chatService.ask(request)));
  }
}
