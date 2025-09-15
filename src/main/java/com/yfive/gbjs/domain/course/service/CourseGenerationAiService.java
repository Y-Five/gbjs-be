/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfive.gbjs.domain.course.dto.request.CourseRequest;
import com.yfive.gbjs.domain.course.dto.response.CourseResponse;
import com.yfive.gbjs.domain.seal.repository.SealSpotRepository;
import com.yfive.gbjs.domain.spot.service.SpotService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** AI 기반 여행 코스 생성 서비스 (버그 수정 + 간단 선정 로직 + 호출 팁 반영) */
@Service
@RequiredArgsConstructor
@Slf4j
public class CourseGenerationAiService {

  private final VectorStore vectorStore;
  private final ChatClient chatClient;
  private final SpotService spotService; // (선택) 후처리/검증 시 사용 예정
  private final SealSpotRepository sealSpotRepository; // (선택)
  private final ObjectMapper objectMapper;

  /** 안전한 문자열 추출 */
  private static String s(Map<String, Object> m, String k) {
    if (m == null) return null;
    Object v = m.get(k);
    return v == null ? null : v.toString();
  }

  /** 안전한 Double 추출 */
  private static Double d(Map<String, Object> m, String k) {
    if (m == null) return null;
    Object v = m.get(k);
    if (v == null) return null;
    try {
      return Double.valueOf(v.toString());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /** 코드펜스/앞뒤 잡음 제거 */
  private static String cleanJsonFences(String raw) {
    if (raw == null) return null;
    String txt = raw.trim();
    if (txt.startsWith("```")) {
      int firstNl = txt.indexOf('\n');
      if (firstNl > 0) {
        txt = txt.substring(firstNl + 1);
      }
    }
    if (txt.endsWith("```")) {
      txt = txt.substring(0, txt.length() - 3);
    }
    return txt.trim();
  }

  /** AI 기반으로 여행 코스를 생성. */
  public CourseResponse.CourseDetailDTO generateAiCourse(
      CourseRequest.CreateCourseRequest request) {
    Objects.requireNonNull(request, "request must not be null");
    Objects.requireNonNull(request.getStartDate(), "startDate must not be null");
    Objects.requireNonNull(request.getEndDate(), "endDate must not be null");
    Objects.requireNonNull(request.getLocations(), "locations must not be null");

    // --- 0) 파라미터/일수 계산 --------------------------------------------
    LocalDate start = request.getStartDate();
    LocalDate end = request.getEndDate();
    long days = ChronoUnit.DAYS.between(start, end) + 1; // 포함 범위
    if (days <= 0) {
      throw new IllegalArgumentException("endDate must be on/after startDate");
    }

    // --- 1) Qdrant에서 관광지 검색 -----------------------------------------
    String query = String.join(" ", request.getLocations()) + " 여행지";
    // 동적 topK: 일수×15, 최소 50, 최대 100
    int topK = Math.max(50, (int) Math.min(100, days * 15));

    List<Document> allRelevantDocuments = new ArrayList<>();
    SearchRequest searchRequest = SearchRequest.builder().query(query).topK(topK).build();
    allRelevantDocuments.addAll(vectorStore.similaritySearch(searchRequest));

    // --- 2) 문서 → DTO 변환 (유효성/타입 필터 + 중복 제거) -------------------
    Map<Long, CourseResponse.SimpleSpotDTO> uniq = new LinkedHashMap<>();
    for (Document doc : allRelevantDocuments) {
      Map<String, Object> md = doc.getMetadata();
      if (md == null) continue;
      String contentIdStr = s(md, "contentId");
      if (contentIdStr == null) continue;
      Long id;
      try {
        id = Long.valueOf(contentIdStr);
      } catch (NumberFormatException e) {
        continue;
      }

      String type = s(md, "type");
      if (!"spot".equals(type) && !"seal_spot".equals(type)) continue; // 허용 타입만

      if (!uniq.containsKey(id)) {
        CourseResponse.SimpleSpotDTO dto =
            CourseResponse.SimpleSpotDTO.builder()
                .spotId(id)
                .name(s(md, "name"))
                .category(s(md, "category"))
                .addr1(s(md, "addr1"))
                .latitude(d(md, "latitude"))
                .longitude(d(md, "longitude"))
                .isSealSpot("seal_spot".equals(type))
                .sealSpotId(
                    "seal_spot".equals(type)
                        ? (s(md, "sealSpotId") == null ? null : Long.valueOf(s(md, "sealSpotId")))
                        : null)
                .build();
        uniq.put(id, dto);
      }
    }
    List<CourseResponse.SimpleSpotDTO> deduped = new ArrayList<>(uniq.values());

    // 좌표/카테고리 누락 항목은 뒤로
    deduped.sort(
        (a, b) -> {
          int p1 =
              (a.getLatitude() == null || a.getLongitude() == null ? 1 : 0)
                  + (a.getCategory() == null ? 1 : 0);
          int p2 =
              (b.getLatitude() == null || b.getLongitude() == null ? 1 : 0)
                  + (b.getCategory() == null ? 1 : 0);
          return Integer.compare(p1, p2);
        });

    List<CourseResponse.SimpleSpotDTO> seal =
        deduped.stream()
            .filter(CourseResponse.SimpleSpotDTO::getIsSealSpot)
            .collect(Collectors.toList());
    List<CourseResponse.SimpleSpotDTO> general =
        deduped.stream().filter(s -> !s.getIsSealSpot()).collect(Collectors.toList());

    // --- 3) 간단 선정 로직 (재현 가능한 셔플 + 비율/상한) ---------------------
    int maxTotal = (int) Math.min(days * 5, 20); // 하루 5개, 전체 20개 상한
    String seedBase = start + "|" + end + "|" + String.join(",", request.getLocations());
    Random rand = new Random(seedBase.hashCode());
    java.util.Collections.shuffle(seal, rand);
    java.util.Collections.shuffle(general, rand);

    int sealTarget = (int) Math.round(maxTotal * 0.4); // 40% 목표
    sealTarget = Math.max(Math.min(sealTarget, seal.size()), Math.min(2, maxTotal)); // 최소 2 보정

    List<CourseResponse.SimpleSpotDTO> spotsForOpenAI = new ArrayList<>();
    if (sealTarget > 0 && !seal.isEmpty()) {
      spotsForOpenAI.addAll(seal.subList(0, Math.min(sealTarget, seal.size())));
    }
    int remain = maxTotal - spotsForOpenAI.size();
    if (remain > 0 && !general.isEmpty()) {
      spotsForOpenAI.addAll(general.subList(0, Math.min(remain, general.size())));
    }
    if (spotsForOpenAI.isEmpty() && !deduped.isEmpty()) {
      spotsForOpenAI.add(deduped.get(0)); // 완전 빈 경우 대비 안전장치
    }

    // --- 4) OpenAI 프롬프트 구성 (문자열 버그 제거: 단 1회 formatted) ---------
    String spotsJson;
    try {
      spotsJson = objectMapper.writeValueAsString(spotsForOpenAI);
    } catch (JsonProcessingException e) {
      log.error("OpenAI 프롬프트용 관광지 목록 JSON 변환 실패", e);
      throw new RuntimeException("AI 프롬프트 준비 실패: " + e.getMessage());
    }

    String prompt =
        """
            You are a helpful travel agent AI. Your task is to create a travel course based on user preferences and a list of available tourist spots.
            The course should be for a trip from %s to %s in the regions of %s.
            Please create a daily itinerary, suggesting up to 5 spots per day.
            Ensure a good mix of general tourist spots and 'seal spots' (띠부씰 관광지).
            The output must be ONLY a JSON object matching the structure of CourseResponse.CourseDetailDTO. Do NOT include any other text, explanations, or markdown formatting.
            Here is the list of available spots (in JSON format):
            %s

            Please generate the course in the following JSON format:
            {
              "title": "Generated Course Title",
              "startDate": "YYYY-MM-DD",
              "endDate": "YYYY-MM-DD",
              "dailyCourses": [
                {
                  "dayNumber": 1,
                  "date": "YYYY-MM-DD",
                  "location": "Region Name",
                  "spots": [
                    {
                      "spotId": 123,
                      "visitOrder": 1,
                      "name": "Spot Name",
                      "category": "Category",
                      "addr1": "Address",
                      "latitude": 36.123,
                      "longitude": 128.456,
                      "isSealSpot": true,
                      "sealSpotId": 789
                    }
                  ]
                }
              ]
            }
            Ensure all fields are correctly populated. For 'spotId', 'name', 'category', 'addr1', 'latitude', 'longitude', 'isSealSpot', 'sealSpotId', use the exact values from the provided available spots list.
            If a spot is not a 'seal spot', set 'isSealSpot' to false and 'sealSpotId' to null.
            Ensure the entire JSON object is complete and syntactically valid.
            """
            .formatted(
                request.getStartDate(),
                request.getEndDate(),
                String.join(", ", request.getLocations()),
                spotsJson);

    log.info(
        "Spots sent to OpenAI ({} total): {}",
        spotsForOpenAI.size(),
        spotsForOpenAI.stream()
            .limit(10)
            .map(s -> s.getName() + "(Seal:" + s.getIsSealSpot() + ")")
            .collect(Collectors.joining(", ")));

    // --- 5) 모델 호출 (버전 호환: 기본 prompt 호출 + 구조화 매핑 시도) -----------
    // NOTE: 일부 Spring AI 버전은 OpenAiChatOptions/ResponseFormat를 코드에서 직접 지정하지 못합니다.
    // 이 경우 application.yml에서 토큰/온도를 설정하세요 (아래 주석 참고).
    try {
      CourseResponse.CourseDetailDTO result =
          chatClient
              .prompt() // 기존 방식 유지 (버전 호환)
              .user(prompt)
              .call()
              .entity(CourseResponse.CourseDetailDTO.class);
      if (result != null) return result;
    } catch (Exception structuredErr) {
      log.warn(
          "Structured mapping failed, falling back to string parsing: {}",
          structuredErr.toString());
    }

    // --- 6) 폴백: 문자열 받아서 JSON 파싱 -------------------------------------
    String aiResponse = chatClient.prompt().user(prompt).call().content();
    log.debug(
        "OpenAI Raw Response (trimmed): {}",
        aiResponse == null ? "null" : aiResponse.substring(0, Math.min(aiResponse.length(), 800)));

    String cleaned = cleanJsonFences(aiResponse);
    try {
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      JsonNode root = objectMapper.readTree(cleaned);
      if (root == null || !root.isObject()) {
        throw new RuntimeException("AI 응답이 유효한 JSON 오브젝트가 아님");
      }
      return objectMapper.readValue(cleaned, CourseResponse.CourseDetailDTO.class);
    } catch (Exception e) {
      log.warn("JSON 1차 파싱 실패, 복구 시도 진행: {}", e.toString());
    }

    // --- 7) 폴백 2: JSON 복구 프롬프트 ---------------------------------------
    String repairPrompt =
        """
            You will be given a possibly truncated JSON. Return ONLY a valid JSON object that matches CourseResponse.CourseDetailDTO.
            - If some array/object tail is cut, remove the incomplete item and close the JSON correctly.
            - Keep existing fields and values as much as possible.
            - Do NOT add explanations or code fences. Only JSON.

            INPUT:
            %s
            """
            .formatted(cleaned);

    String repaired = chatClient.prompt().user(repairPrompt).call().content();
    String repairedClean = cleanJsonFences(repaired);
    try {
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      JsonNode root2 = objectMapper.readTree(repairedClean);
      if (root2 == null || !root2.isObject()) {
        throw new RuntimeException("복구 응답이 유효한 JSON 오브젝트가 아님");
      }
      return objectMapper.readValue(repairedClean, CourseResponse.CourseDetailDTO.class);
    } catch (Exception e2) {
      log.error(
          "JSON 복구 실패. 원본/복구 응답 일부: orig={}, repaired={}",
          aiResponse == null ? "null" : aiResponse.substring(0, Math.min(aiResponse.length(), 400)),
          repaired == null ? "null" : repaired.substring(0, Math.min(repaired.length(), 400)),
          e2);
      throw new RuntimeException("AI 코스 생성 실패: " + e2.getMessage());
    }
  }
}
