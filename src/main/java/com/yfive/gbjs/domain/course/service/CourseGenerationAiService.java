/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfive.gbjs.domain.course.dto.request.CourseRequest;
import com.yfive.gbjs.domain.course.dto.response.CourseResponse;
import com.yfive.gbjs.domain.seal.repository.SealSpotRepository;
import com.yfive.gbjs.domain.spot.service.SpotService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AI 기반 여행 코스 생성 비즈니스 로직을 처리하는 서비스 클래스입니다. Qdrant에서 관광지 정보를 검색하고, OpenAI를 활용하여 사용자 요청에 맞는 코스를
 * 생성합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CourseGenerationAiService {

  private final VectorStore vectorStore;
  private final ChatClient chatClient;
  private final SpotService spotService;
  private final SealSpotRepository sealSpotRepository;
  private final ObjectMapper objectMapper;

  /**
   * AI 기반으로 여행 코스를 생성합니다. 사용자의 여행 요청(날짜, 지역)을 기반으로 Qdrant에서 관련 관광지를 검색하고, OpenAI 모델을 활용하여 최적의 코스를
   * 구성합니다.
   *
   * @param request 코스 생성 요청 정보 (시작일, 종료일, 지역 목록)
   * @return AI가 생성한 코스 상세 정보
   */
  public CourseResponse.CourseDetailDTO generateAiCourse(
      CourseRequest.CreateCourseRequest request) {

    // 1. Qdrant에서 관광지 정보 검색
    // 사용자가 요청한 지역을 기반으로 Qdrant에 질의할 쿼리 문자열을 생성합니다.
    String query = String.join(" ", request.getLocations()) + " 여행지";
    // Qdrant에서 검색할 문서의 최대 개수를 설정합니다. (더 많은 후보군 확보)
    int topK = 50;

    List<Document> allRelevantDocuments = new java.util.ArrayList<>();

    // Qdrant에서 모든 관련 문서를 검색합니다. (타입 필터 없이)
    // 검색된 문서들은 Qdrant에 저장된 관광지 정보(일반 spot, 띠부씰 spot 등)입니다.
    SearchRequest searchRequest = SearchRequest.builder().query(query).topK(topK).build();
    allRelevantDocuments.addAll(vectorStore.similaritySearch(searchRequest));

    // 검색된 문서들을 SimpleSpotDTO로 변환하고, 유효한 관광지(contentId 존재) 및
    // 'spot' 또는 'seal_spot' 타입만 필터링하여 선택합니다.
    List<CourseResponse.SimpleSpotDTO> selectedSpots =
        allRelevantDocuments.stream()
            .filter(doc -> doc.getMetadata().containsKey("contentId")) // contentId가 있는 유효한 문서만 선택
            .filter(
                doc -> { // Java 코드 내에서 타입 필터링
                  Object type = doc.getMetadata().get("type");
                  return "spot".equals(type)
                      || "seal_spot".equals(type); // 'spot' 또는 'seal_spot' 타입만 포함
                })
            .map(
                doc -> {
                  // Qdrant Document의 페이로드(메타데이터)를 SimpleSpotDTO 객체로 매핑합니다.
                  Map<String, Object> metadata = doc.getMetadata();
                  return CourseResponse.SimpleSpotDTO.builder()
                      .spotId(Long.valueOf(metadata.get("contentId").toString()))
                      .name(metadata.get("name").toString())
                      .category(metadata.get("category").toString())
                      .addr1(metadata.get("addr1").toString())
                      .latitude(
                          metadata.containsKey("latitude")
                              ? Double.valueOf(metadata.get("latitude").toString())
                              : null)
                      .longitude(
                          metadata.containsKey("longitude")
                              ? Double.valueOf(metadata.get("longitude").toString())
                              : null)
                      .isSealSpot(
                          "seal_spot"
                              .equals(
                                  metadata.get("type"))) // 'type' 메타데이터가 'seal_spot'이면 띠부씰 관광지로 간주
                      .sealSpotId(
                          metadata.containsKey("sealSpotId")
                              ? Long.valueOf(metadata.get("sealSpotId").toString())
                              : null) // 띠부씰 관광지인 경우 sealSpotId 포함
                      .build();
                })
            .collect(Collectors.toList());

    // Implement logic to select an "appropriate mix" from selectedSpots
    // 2. 코스에 포함할 관광지 선정 및 혼합 로직
    // 검색된 관광지들을 일반 관광지와 띠부씰 관광지로 분리합니다.
    List<CourseResponse.SimpleSpotDTO> generalSpots =
        selectedSpots.stream()
            .filter(spot -> !spot.getIsSealSpot()) // 띠부씰 관광지가 아닌 경우
            .collect(Collectors.toList());

    List<CourseResponse.SimpleSpotDTO> sealSpots =
        selectedSpots.stream()
            .filter(CourseResponse.SimpleSpotDTO::getIsSealSpot) // 띠부씰 관광지인 경우
            .collect(Collectors.toList());

    List<CourseResponse.SimpleSpotDTO> spotsForOpenAI = new java.util.ArrayList<>();

    // 띠부씰 관광지가 존재한다면, 그 수의 절반을 코스에 포함합니다.
    int numSealSpotsToInclude = sealSpots.size() / 2;
    if (numSealSpotsToInclude > 0) {
      // OpenAI 프롬프트에 전달할 최대 스팟 개수(20개)를 초과하지 않도록 조정합니다.
      numSealSpotsToInclude = Math.min(numSealSpotsToInclude, 20);
      spotsForOpenAI.addAll(sealSpots.subList(0, numSealSpotsToInclude));
    }

    // 남은 공간을 일반 관광지로 채웁니다. (총 20개까지)
    int remainingCapacity = 20 - spotsForOpenAI.size();
    if (remainingCapacity > 0) {
      // 남은 용량과 일반 관광지 개수 중 더 작은 값만큼 추가합니다.
      int numGeneralSpotsToInclude = Math.min(remainingCapacity, generalSpots.size());
      spotsForOpenAI.addAll(generalSpots.subList(0, numGeneralSpotsToInclude));
    }

    // OpenAI 프롬프트에 전달하기 전에 스팟 목록을 무작위로 섞어,
        // AI가 특정 순서에 편향되지 않도록 합니다.
        java.util.Collections.shuffle(spotsForOpenAI);

        log.info("Spots sent to OpenAI: {}", spotsForOpenAI.stream().map(s -> s.getName() + " (Seal: " + s.getIsSealSpot() + ")").collect(Collectors.joining(", ")));

        // 3. OpenAI 프롬프트 구성
        // OpenAI 모델에 전달할 관광지 목록을 JSON 문자열로 변환합니다.
        String spotsJson;
        try {
            spotsJson = objectMapper.writeValueAsString(spotsForOpenAI);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("OpenAI 프롬프트용 관광지 목록 JSON 변환 실패", e);
            throw new RuntimeException("AI 프롬프트 준비 실패: " + e.getMessage());
        }

    // OpenAI 모델에게 전달할 상세 프롬프트를 구성합니다.
    // 역할, 지침, 사용자 요청, 사용 가능한 관광지 목록, 그리고 응답 형식에 대한 명확한 지시를 포함합니다.
    String prompt =
        """
            You are a helpful travel agent AI. Your task is to create a travel course based on user preferences and a list of available tourist spots.
            The course should be for a trip from %s to %s in the regions of %s.
            Please create a daily itinerary, suggesting up to 5 spots per day.
            Ensure a good mix of general tourist spots and 'seal spots' (띠부씰 관광지).
            The output must be ONLY a JSON object matching the structure of CourseResponse.CourseDetailDTO. Do NOT include any other text, explanations, or markdown formatting (e.g., ```json).
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
                        If a spot is not a 'seal spot', set 'isSealSpot' to false and 'sealSpotId' to null.            Ensure the entire JSON object is complete and syntactically valid.
            """.formatted(
                request.getStartDate(),
                request.getEndDate(),
                String.join(", ", request.getLocations()),
                spotsJson
            );""".formatted(
                request.getStartDate(),
                request.getEndDate(),
                String.join(", ", request.getLocations()),
                spotsJson
            );
            Ensure the entire JSON object is complete and syntactically valid."
            """
            .formatted(
                request.getStartDate(),
                request.getEndDate(),
                String.join(", ", request.getLocations()),
                spotsJson);

    // 4. OpenAI 호출 및 응답 파싱
    // 구성된 프롬프트를 OpenAI 모델로 전송하고 응답을 받습니다.
    String aiResponse = chatClient.prompt().user(prompt).call().content();
    log.info("OpenAI Raw Response: {}", aiResponse); // 원시 응답 로그 기록

    // OpenAI 응답 전처리: JSON 마크다운 블록 제거 및 불필요한 텍스트 제거
    String cleanedAiResponse = aiResponse.trim();
    if (cleanedAiResponse.startsWith("```json")) {
      cleanedAiResponse = cleanedAiResponse.substring("```json".length());
      if (cleanedAiResponse.endsWith("```")) {
        cleanedAiResponse =
            cleanedAiResponse.substring(0, cleanedAiResponse.length() - "```".length());
      }
    }
    cleanedAiResponse = cleanedAiResponse.trim(); // 최종 공백 제거

    try {
      // OpenAI의 JSON 응답을 CourseResponse.CourseDetailDTO 객체로 파싱합니다.
      return objectMapper.readValue(cleanedAiResponse, CourseResponse.CourseDetailDTO.class);
    } catch (Exception e) {
      log.error("OpenAI 응답을 CourseDetailDTO로 파싱 실패 (원시 응답: {})", aiResponse, e); // 원시 응답도 로그에 포함
      // 파싱 실패 시 런타임 예외를 발생시켜 상위 호출자에게 알립니다.
      throw new RuntimeException("AI 코스 생성 실패: " + e.getMessage());
    }
  }
}
