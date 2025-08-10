/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tts.service;

import com.yfive.gbjs.domain.tts.dto.request.TtsRequest;
import com.yfive.gbjs.domain.tts.dto.response.TtsResponse;
import com.yfive.gbjs.domain.tts.entity.Gender;
import com.yfive.gbjs.domain.tts.entity.SpeechType;

public interface TtsService {

  TtsResponse convertTextToSpeech(
      Long guideId, Gender gender, SpeechType speechType, TtsRequest request);
}
