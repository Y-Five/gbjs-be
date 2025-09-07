/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.chat.service;

import com.yfive.gbjs.domain.chat.dto.request.ChatRequest;

public interface ChatService {

  String ask(ChatRequest request);
}
