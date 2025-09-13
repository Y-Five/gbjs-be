/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yfive.gbjs.domain.course.converter.CourseConverter;
import com.yfive.gbjs.domain.course.dto.request.CourseRequest.CreateCourseRequest;
import com.yfive.gbjs.domain.course.dto.request.CourseRequest.SaveCourseRequest;
import com.yfive.gbjs.domain.course.dto.response.CourseResponse;
import com.yfive.gbjs.domain.course.entity.Course;
import com.yfive.gbjs.domain.course.entity.DailyCourse;
import com.yfive.gbjs.domain.course.entity.mapper.DailyCourseSpot;
import com.yfive.gbjs.domain.course.exception.CourseErrorStatus;
import com.yfive.gbjs.domain.course.repository.CourseRepository;
import com.yfive.gbjs.domain.course.repository.DailyCourseRepository;
import com.yfive.gbjs.domain.course.repository.DailyCourseSpotRespository;
import com.yfive.gbjs.domain.seal.entity.Location;
import com.yfive.gbjs.domain.seal.entity.Seal;
import com.yfive.gbjs.domain.seal.entity.SealSpot;
import com.yfive.gbjs.domain.seal.entity.SortBy;
import com.yfive.gbjs.domain.seal.repository.SealRepository;
import com.yfive.gbjs.domain.seal.repository.SealSpotRepository;
import com.yfive.gbjs.domain.seal.repository.UserSealRepository;
import com.yfive.gbjs.domain.user.entity.User;
import com.yfive.gbjs.domain.user.exception.UserErrorStatus;
import com.yfive.gbjs.domain.user.repository.UserRepository;
import com.yfive.gbjs.global.error.exception.CustomException;

import lombok.RequiredArgsConstructor;

/** 여행 코스 서비스 구현체 코스 생성, 저장, 조회, 삭제 등의 비즈니스 로직을 처리합니다. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

  private final CourseRepository courseRepository;
  private final SealSpotRepository sealSpotRepository;
  private final UserRepository userRepository;
  private final CourseConverter courseConverter;
  private final UserSealRepository userSealRepository;
  private final SealRepository sealRepository;
  private final DailyCourseSpotRespository dailyCourseSpotRespository;
  private final DailyCourseRepository dailyCourseRepository;

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
          SealSpot sealSpot = sealSpots.get(i);
          spotDTOs.add(
              CourseResponse.SimpleSpotDTO.builder()
                  .spotId(sealSpot.getSpotId())
                  .visitOrder(i + 1)
                  .name(sealSpot.getName())
                  .category(
                      sealSpot.getCategory() != null
                          ? courseConverter.getCategoryKoreanName(sealSpot.getCategory())
                          : null)
                  .addr1(sealSpot.getAddr1())
                  .latitude(sealSpot.getLatitude())
                  .longitude(sealSpot.getLongitude())
                  .isSealSpot(true)
                  .sealSpotId(sealSpot.getId())
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
        courseRepository.save(
            Course.builder()
                .user(user)
                .title(title)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build());

    // 요청에 포함된 일차별 코스 및 관광지 정보로 저장
    for (SaveCourseRequest.DailyCourseRequest dailyCourseRequest : request.getDailyCourses()) {
      Location location =
          courseConverter.getLocationFromKoreanName(dailyCourseRequest.getLocation());

      DailyCourse dailyCourse =
          dailyCourseRepository.save(
              DailyCourse.builder()
                  .dayNumber(dailyCourseRequest.getDayNumber())
                  .date(dailyCourseRequest.getDate())
                  .location(location)
                  .course(course)
                  .build());

      if (dailyCourseRequest.getSpots() != null) {
        for (SaveCourseRequest.SpotRequest spotRequest : dailyCourseRequest.getSpots()) {
          SealSpot sealSpot =
              sealSpotRepository
                  .findById(spotRequest.getSealSpotId())
                  .orElseThrow(() -> new CustomException(CourseErrorStatus._SPOT_NOT_FOUND));

          DailyCourseSpot dailyCourseSpot =
              dailyCourseSpotRespository.save(
                  DailyCourseSpot.builder()
                      .sealSpot(sealSpot)
                      .spotId(spotRequest.getSpotId())
                      .visitOrder(spotRequest.getVisitOrder())
                      .latitude(spotRequest.getLatitude())
                      .longitude(spotRequest.getLongitude())
                      .dailyCourse(dailyCourse)
                      .build());
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
  public CourseResponse.CourseListDTO getUserCourses(
      Long userId, List<String> locationNames, SortBy sortBy) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorStatus.USER_NOT_FOUND));

    List<Course> courses = courseRepository.findByUser(user);

    // 지역명 필터링 적용
    if (locationNames != null && !locationNames.isEmpty()) {
      courses =
          courses.stream()
              .filter(
                  course ->
                      course.getDailyCourses().stream()
                          .anyMatch(
                              dailyCourse ->
                                  locationNames.contains(
                                      courseConverter.getLocationKoreanName(
                                          dailyCourse.getLocation()))))
              .collect(Collectors.toList());
    }

    List<CourseResponse.CourseSummaryDTO> summaries =
        courses.stream()
            .sorted(getCourseComparator(sortBy))
            .map(
                course -> {
                  int totalCollectableSealsForCourse = 0;
                  int userCollectedSealsForCourse = 0;

                  List<Long> sealSpotIds =
                      course.getDailyCourses().stream()
                          .flatMap(dailyCourse -> dailyCourse.getSpots().stream())
                          .filter(dailyCourseSpot -> dailyCourseSpot.getSealSpot() != null)
                          .map(dailyCourseSpot -> dailyCourseSpot.getSealSpot().getId())
                          .distinct()
                          .collect(Collectors.toList());

                  for (Long sealSpotId : sealSpotIds) {
                    List<Seal> sealsFromSpot = sealRepository.findBySealSpot_Id(sealSpotId);
                    totalCollectableSealsForCourse += sealsFromSpot.size();
                    userCollectedSealsForCourse +=
                        userSealRepository.countByUserAndSealIn(user, sealsFromSpot);
                  }

                  return courseConverter.toCourseSummaryDTO(
                      course, totalCollectableSealsForCourse, userCollectedSealsForCourse);
                })
            .collect(Collectors.toList());

    return CourseResponse.CourseListDTO.builder()
        .courses(summaries)
        .totalCount(summaries.size())
        .build();
  }

  private java.util.Comparator<Course> getCourseComparator(SortBy sortBy) {
    switch (sortBy) {
      case OLDEST:
        return java.util.Comparator.comparing(Course::getCreatedAt);
      case LATEST:
      default:
        return java.util.Comparator.comparing(Course::getCreatedAt).reversed();
    }
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
    String simplifiedLocationNames =
        locations.stream()
            .limit(3)
            .map(courseConverter::getLocationKoreanName)
            .map(name -> name.replaceAll("[시군]$", ""))
            .collect(Collectors.joining(", "));

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
}
