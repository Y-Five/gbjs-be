/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfive.gbjs.domain.course.converter.CourseConverter;
import com.yfive.gbjs.domain.course.dto.request.CourseRequest.CreateCourseRequest;
import com.yfive.gbjs.domain.course.dto.request.CourseRequest.SaveCourseRequest;
import com.yfive.gbjs.domain.course.dto.response.CourseResponse;
import com.yfive.gbjs.domain.course.dto.response.CourseResponse.CourseDetailDTO;
import com.yfive.gbjs.domain.course.dto.response.CourseResponse.DailyCourseDTO;
import com.yfive.gbjs.domain.course.dto.response.CourseResponse.SimpleSpotDTO;
import com.yfive.gbjs.domain.course.entity.*;
import com.yfive.gbjs.domain.course.entity.mapper.DailyCourseSpot;
import com.yfive.gbjs.domain.course.exception.CourseErrorStatus;
import com.yfive.gbjs.domain.course.repository.CourseRepository;
import com.yfive.gbjs.domain.seal.entity.Location;
import com.yfive.gbjs.domain.seal.entity.SealSpot;
import com.yfive.gbjs.domain.seal.repository.SealSpotRepository;
import com.yfive.gbjs.domain.user.entity.User;
import com.yfive.gbjs.domain.user.exception.UserErrorStatus;
import com.yfive.gbjs.domain.user.repository.UserRepository;
import com.yfive.gbjs.global.error.exception.CustomException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 여행 코스 서비스 구현체 코스 생성, 저장, 조회, 삭제 등의 비즈니스 로직을 처리합니다. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CourseServiceImpl implements CourseService {

  private final CourseRepository courseRepository;
  private final SealSpotRepository sealSpotRepository;
  private final UserRepository userRepository;
  private final CourseConverter courseConverter;
  private final VectorStore vectorStore;
  private final ChatClient chatClient;
  private final ObjectMapper objectMapper; // ObjectMapper 추가

  /**
   * 여행 코스를 생성합니다. (DB 저장하지 않음) - 날짜 유효성 검증 - 자동으로 제목 생성 (예: "경주, 포항 2일 여행") - 각 일차별로 지역 분배 - 지역별
   * 관광지를 랜덤으로 최대 5개씩 선택
   */
  @Override
  public CourseResponse.CourseDetailDTO generateCourse(CreateCourseRequest request) {
    long totalDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

    if (totalDays < 1) {
      throw new CustomException(CourseErrorStatus._INVALID_DATE_RANGE);
    }

    // 한글 지역명을 Location enum으로 변환
    List<Location> locationEnums =
        request.getLocations().stream()
            .map(courseConverter::getLocationFromKoreanName)
            .collect(Collectors.toList());

    String title = generateTitle(locationEnums, totalDays);

    List<CourseResponse.DailyCourseDTO> dailyCourses = new ArrayList<>();
    List<Location> locations = distributeLocations(locationEnums, (int) totalDays);

    for (int dayNum = 1; dayNum <= totalDays; dayNum++) {
      LocalDate date = request.getStartDate().plusDays(dayNum - 1);
      Location location = locations.get(dayNum - 1);

      List<SealSpot> sealSpots = sealSpotRepository.findByLocation(location);
      List<CourseResponse.SimpleSpotDTO> spotDTOs = new ArrayList<>();

      if (!sealSpots.isEmpty()) {
        Collections.shuffle(sealSpots);
        int spotsPerDay = Math.min(sealSpots.size(), 5);

        for (int i = 0; i < spotsPerDay; i++) {
          SealSpot sealSpot =
              sealSpots.get(i);
          spotDTOs.add(
              CourseResponse.SimpleSpotDTO.builder()
                  .spotId(sealSpot.getId())
                  .visitOrder(i + 1)
                  .name(sealSpot.getName())
                  .category(
                      sealSpot.getCategory() != null
                          ? courseConverter.getCategoryKoreanName(sealSpot.getCategory())
                          : null)
                  .build());
        }
      }

      dailyCourses.add(
          CourseResponse.DailyCourseDTO.builder()
              .dayNumber(dayNum)
              .date(date)
              .location(courseConverter.getLocationKoreanName(location))
              .spots(spotDTOs)
              .build());
    }

    return CourseResponse.CourseDetailDTO.builder()
        .title(title)
        .startDate(request.getStartDate())
        .endDate(request.getEndDate())
        .dailyCourses(dailyCourses)
        .build();
  }

  /**
   * 생성된 코스를 DB에 저장합니다. - 사용자 존재 여부 확인 - Course 엔티티 생성 및 저장 - DailyCourse와 DailyCourseSpot 관계 설정 -
   * 영속성 전이(Cascade)로 연관 엔티티 자동 저장
   */
  @Override
  @Transactional
  public CourseResponse.CourseDetailDTO saveCourse(Long userId, SaveCourseRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorStatus.USER_NOT_FOUND));

    long totalDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

    if (totalDays < 1) {
      throw new CustomException(CourseErrorStatus._INVALID_DATE_RANGE);
    }

    // 제목이 없으면 자동 생성
    String title = request.getTitle();
    if (title == null || title.trim().isEmpty()) {
      // 일차별 코스에서 지역 명 추출
      List<Location> locations =
          request.getDailyCourses().stream()
              .map(dc -> courseConverter.getLocationFromKoreanName(dc.getLocation()))
              .distinct()
              .collect(Collectors.toList());
      title = generateTitle(locations, totalDays);
    }

    Course course =
        Course.builder()
            .user(user)
            .title(title)
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .build();

    // 요청에 포함된 일차별 코스 및 관광지 정보로 저장
    for (SaveCourseRequest.DailyCourseRequest dailyCourseRequest : request.getDailyCourses()) {
      Location location =
          courseConverter.getLocationFromKoreanName(dailyCourseRequest.getLocation());

      DailyCourse dailyCourse =
          DailyCourse.builder()
              .dayNumber(dailyCourseRequest.getDayNumber())
              .date(dailyCourseRequest.getDate())
              .location(location)
              .build();

      if (dailyCourseRequest.getSpots() != null) {
        for (SaveCourseRequest.SpotRequest spotRequest : dailyCourseRequest.getSpots()) {
          SealSpot sealSpot =
              sealSpotRepository
                  .findById(spotRequest.getSpotId())
                  .orElseThrow(() -> new CustomException(CourseErrorStatus._SPOT_NOT_FOUND));

          DailyCourseSpot dailyCourseSpot =
              DailyCourseSpot.builder()
                  .sealSpot(sealSpot)
                  .visitOrder(spotRequest.getVisitOrder())
                  .build();
          dailyCourse.addSpot(dailyCourseSpot);
        }
      }

      course.addDailyCourse(dailyCourse);
    }

    Course savedCourse = courseRepository.save(course);
    return courseConverter.toCourseDetailDTO(savedCourse);
  }

  /** 특정 코스의 상세 정보를 조회합니다. - 코스 존재 여부 확인 - 본인 코스인지 권한 검증 */
  @Override
  public CourseResponse.CourseDetailDTO getCourse(Long userId, Long courseId) {
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new CustomException(CourseErrorStatus._COURSE_NOT_FOUND));

    // 본인의 코스인지 확인
    if (!course.getUser().getId().equals(userId)) {
      throw new CustomException(UserErrorStatus.UNAUTHORIZED);
    }

    return courseConverter.toCourseDetailDTO(course);
  }

  /** 사용자의 모든 코스 목록을 조회합니다. - 시작일 기준 내림차순 정렬 - 코스 요약 정보만 반환 (상세 정보 제외) */
  @Override
  public CourseResponse.CourseListDTO getUserCourses(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorStatus.USER_NOT_FOUND));

    List<Course> courses = courseRepository.findByUserOrderByStartDateDesc(user);

    List<CourseResponse.CourseSummaryDTO> summaries =
        courses.stream().map(courseConverter::toCourseSummaryDTO).collect(Collectors.toList());

    return CourseResponse.CourseListDTO.builder()
        .courses(summaries)
        .totalCount(summaries.size())
        .build();
  }

  /**
   * 코스를 삭제합니다. - 코스 존재 여부 확인 - 본인 코스인지 권한 검증 - 연관된 DailyCourse, DailyCourseSpot도 함께 삭제
   * (orphanRemoval)
   */
  @Override
  @Transactional
  public void deleteCourse(Long userId, Long courseId) {
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new CustomException(CourseErrorStatus._COURSE_NOT_FOUND));

    // 본인의 코스인지 확인
    if (!course.getUser().getId().equals(userId)) {
      throw new CustomException(UserErrorStatus.UNAUTHORIZED);
    }

    courseRepository.deleteById(courseId);
  }

  /**
   * 코스 제목을 자동 생성합니다.
   *
   * @param locations 방문 지역 목록
   * @param days 여행 일수
   * @return "지역1, 지역2, 지역3 N일 여행" 형식의 제목
   */
  private String generateTitle(List<Location> locations, long days) {
    String locationNames =
        locations.stream()
            .limit(3)
            .map(courseConverter::getLocationKoreanName)
            .collect(Collectors.joining(", "));

    // "시", "군" 제거하고 지역명만 사용
    String simplifiedLocationNames = locationNames.replace("시", "").replace("군", "");
    return String.format("%s %d일 여행", simplifiedLocationNames, days);
  }

  /**
   * 여행 일수에 맞춰 지역을 분배합니다.
   *
   * @param locations 선택된 지역 목록
   * @param days 여행 일수
   * @return 각 일차에 할당된 지역 목록 (순환 반복)
   */
  private List<Location> distributeLocations(List<Location> locations, int days) {
    List<Location> distributed = new ArrayList<>();

    for (int i = 0; i < days; i++) {
      distributed.add(locations.get(i % locations.size()));
    }

    return distributed;
  }

  /**
   * 사용자의 요청을 기반으로 AI를 통해 여행 코스를 생성합니다.
   *
   * @param request 사용자의 여행 기준 (지역, 기간 등)
   * @return AI와 DB 데이터를 조합하여 완성된 여행 코스 DTO
   */
  public CourseResponse.CourseDetailDTO generateCourseByAI(CreateCourseRequest request) {
    log.info("AI 코스 생성 요청: {}", request);

    // --- 사전 준비 ---
    long totalDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
    String locationsString = String.join(",", request.getLocations());

    // 1. 제목은 백엔드에서 직접, 확정적으로 생성
    String finalTitle = String.format("%d월 %d일 %s 코스",
            request.getStartDate().getMonthValue(),
            request.getStartDate().getDayOfMonth(),
            locationsString);

    String query = String.format("%s 지역의 %d일간의 여행", locationsString, totalDays);

    // 2. Qdrant에서 후보 정보 '한 번만' 검색하고, Map에 보관
    List<Document> documents = vectorStore.similaritySearch(
            SearchRequest.builder().query(query).topK(15).build() // 후보군을 넉넉하게 검색
    );

    if (documents.isEmpty()) {
      log.warn("Qdrant에서 관련 문서를 찾을 수 없음: {}", query);
      throw new CustomException(CourseErrorStatus._COURSE_NOT_FOUND);
    }

    // AI 답변과 매칭할 원본 데이터 지도(Map) 생성
    Map<String, Document> spotLookupMap = documents.stream()
            .collect(Collectors.toMap(
                    doc -> (String) doc.getMetadata().get("name"),
                    doc -> doc,
                    (doc1, doc2) -> doc1 // 중복 이름이 있을 경우 첫 번째 문서 사용
            ));

    // 3. AI에게 전달할 관광지 이름 목록만 간단히 준비
    String spotNamesForAI = String.join(", ", spotLookupMap.keySet());

    // --- AI 호출 ---
    String systemMessageContent = String.format("""
            당신은 주어진 관광지 목록을 %d일 동안 방문할 최적의 순서로 나누는 여행 스케줄러입니다.
            - 각 날짜에 방문할 관광지 이름을 순서대로 배열에 담아주세요.
            - 다른 설명 없이 JSON 객체만 반환해주세요. key는 일차(day number)입니다.
            - 예시 형식: { "1": ["장소A", "장소B"], "2": ["장소C", "장소D"] }
            """, totalDays);

    String userMessageContent = String.format("[관광지 목록]\n%s", spotNamesForAI);

    Prompt prompt = new Prompt(List.of(
            new SystemMessage(systemMessageContent),
            new UserMessage(userMessageContent)
    ));

    try {
      String aiResponseJson = chatClient.prompt(prompt).call().content();
      log.info("AI 스케줄링 완료: {}", aiResponseJson);

      // 최종 데이터 조립
      JsonNode rootNode = objectMapper.readTree(aiResponseJson);
      List<DailyCourseDTO> dailyCourses = new ArrayList<>();

      // AI가 배정한 순서에 따라 최종 데이터 조립
      Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> entry = fields.next();
        int dayNumber = Integer.parseInt(entry.getKey());
        LocalDate date = request.getStartDate().plusDays(dayNumber - 1);

        List<SimpleSpotDTO> spots = new ArrayList<>();
        int visitOrder = 1;

        for (JsonNode spotNameNode : entry.getValue()) {
          String spotNameFromAI = spotNameNode.asText();
          Document originalDoc = spotLookupMap.get(spotNameFromAI);

          if (originalDoc != null) {
            Map<String, Object> metadata = originalDoc.getMetadata();

            // 원본 메타데이터에서 신뢰할 수 있는 ID와 '사실' 정보를 가져옴
            Long spotId = (Long) metadata.get("contentId"); // 실제 ID 필드명
            String category = (String) metadata.get("category"); // 실제 카테고리 필드명
            boolean isSealSpot = "seal_spot".equals(metadata.get("entity_type"));
            Long sealSpotId = isSealSpot ? (Long) metadata.get("sealSpotId") : null;

            spots.add(SimpleSpotDTO.builder()
                    .spotId(spotId)
                    .name(spotNameFromAI)
                    // .description() // 설명이 필요 없으므로 최종 DTO에서도 제거
                    .category(category)
                    .isSealSpot(isSealSpot)
                    .sealSpotId(sealSpotId)
                    .visitOrder(visitOrder++)
                    .build());
          } else {
            log.warn("AI가 응답한 장소 '{}'를 원본 목록에서 찾을 수 없습니다.", spotNameFromAI);
          }
        }

        if (!spots.isEmpty()) {
          dailyCourses.add(DailyCourseDTO.builder()
                  .dayNumber(dayNumber)
                  .date(date)
                  .location( (String) spotLookupMap.get(spots.get(0).getName()).getMetadata().get("location")) // 첫 장소의 지역명을 대표로 사용
                  .spots(spots)
                  .build());
        }
      }

      // AI가 날짜 순서를 보장하지 않을 수 있으므로, dayNumber 기준으로 정렬
      dailyCourses.sort(Comparator.comparingInt(DailyCourseDTO::getDayNumber));

      return CourseDetailDTO.builder()
              .title(finalTitle) // 직접 만든 제목 사용
              .startDate(request.getStartDate())
              .endDate(request.getEndDate())
              .dailyCourses(dailyCourses)
              .build();

    } catch (Exception e) {
      log.error("AI 코스 생성 및 파싱 중 오류 발생", e);
      throw new CustomException(CourseErrorStatus._COURSE_GENERATION_FAILED);
    }
  }
}