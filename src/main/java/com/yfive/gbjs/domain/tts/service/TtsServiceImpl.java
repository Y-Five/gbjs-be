/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tts.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yfive.gbjs.domain.guide.entity.AudioGuide;
import com.yfive.gbjs.domain.guide.exception.GuideErrorStatus;
import com.yfive.gbjs.domain.guide.repository.AudioGuideRepository;
import com.yfive.gbjs.domain.tts.dto.request.TtsRequest;
import com.yfive.gbjs.domain.tts.entity.AudioFile;
import com.yfive.gbjs.domain.tts.entity.TtsSetting;
import com.yfive.gbjs.domain.tts.exception.TtsErrorStatus;
import com.yfive.gbjs.domain.tts.repository.TtsRepository;
import com.yfive.gbjs.global.error.exception.CustomException;
import com.yfive.gbjs.global.s3.entity.PathName;
import com.yfive.gbjs.global.s3.service.S3Service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TtsServiceImpl implements TtsService {

  @Value("${google.tts.api-key}")
  private String apiKey;

  @Value("${google.tts.api-url}")
  private String apiUrl;

  private final RestClient restClient;
  private final S3Service s3Service;
  private final TtsRepository ttsRepository;
  private final AudioGuideRepository audioGuideRepository;

  @Override
  @Transactional
  public String convertTextToSpeech(Long guideId, TtsSetting ttsSetting, TtsRequest request) {

    StringBuilder type = new StringBuilder("ko-KR-Standard-");
    String ssmlGender = "FEMALE";

    switch (ttsSetting) {
      case FEMALE_A -> type.append("A");
      case FEMALE_B -> type.append("B");
      case MALE_C -> {
        type.append("C");
        ssmlGender = "MALE";
      }
      case MALE_D -> {
        type.append("D");
        ssmlGender = "MALE";
      }
    }

    try {
      ObjectMapper mapper = new ObjectMapper();

      ObjectNode root = mapper.createObjectNode();

      ObjectNode inputNode = root.putObject("input");
      inputNode.put("text", request.getScript());

      ObjectNode voiceNode = root.putObject("voice");
      voiceNode.put("languageCode", "ko-KR");
      voiceNode.put("ssmlGender", ssmlGender);
      voiceNode.put("name", type.toString());

      ObjectNode audioConfigNode = root.putObject("audioConfig");
      audioConfigNode.put("audioEncoding", "MP3");
      audioConfigNode.put("speakingRate", 1.3);
      audioConfigNode.put("pitch", 0.0);

      String requestBody = mapper.writeValueAsString(root);

      GoogleTtsResponse response =
          restClient
              .post()
              .uri(apiUrl + apiKey)
              .header("Content-Type", "application/json")
              .body(requestBody)
              .retrieve()
              .toEntity(GoogleTtsResponse.class)
              .getBody();

      if (response != null && response.getAudioContent() != null) {
        byte[] audioData = Base64.getDecoder().decode(response.getAudioContent());
        InputStream inputStream = new ByteArrayInputStream(audioData);
        String s3Url =
            s3Service.uploadFile(PathName.AUDIO, guideId, inputStream, "tts.mp3", "audio/mpeg");

        AudioGuide audioGuide =
            audioGuideRepository
                .findById(guideId)
                .orElseThrow(() -> new CustomException(GuideErrorStatus.AUDIO_GUIDE_NOT_FOUND));

        AudioFile audioFile =
            AudioFile.builder()
                .type(type.substring(type.lastIndexOf("-") + 1))
                .fileUrl(s3Url)
                .audioGuide(audioGuide)
                .build();

        audioGuide.addAudioFile(audioFile);
        ttsRepository.save(audioFile);

        log.info("TTS 오디오 생성 완료, S3 URL: {}", s3Url);
        return s3Url;
      } else {
        log.error("Google TTS API 호출 실패: 응답이 비어있음");
        throw new CustomException(TtsErrorStatus.TTS_API_ERROR);
      }
    } catch (Exception e) {
      log.error("TTS 변환 중 예외 발생", e);
      throw new CustomException(TtsErrorStatus.TTS_API_ERROR);
    }
  }

  @Override
  public String getTextToSpeech(Long guideId) {

    return s3Service.getFile(PathName.AUDIO, guideId);
  }

  @Setter
  @Getter
  private static class GoogleTtsResponse {

    private String audioContent;
  }
}
