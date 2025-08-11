/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tts.service;

import com.yfive.gbjs.domain.tts.dto.request.TtsRequest;
import com.yfive.gbjs.domain.tts.entity.TtsSetting;

public interface TtsService {

  String convertTextToSpeech(Long guideId, TtsSetting ttsSetting, TtsRequest request);

  String getTextToSpeech(Long fileId);
}
