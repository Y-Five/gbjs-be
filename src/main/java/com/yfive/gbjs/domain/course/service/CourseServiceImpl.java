/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.service;

import com.yfive.gbjs.domain.course.dto.request.CreateCourseRequest;
import com.yfive.gbjs.domain.course.dto.request.SaveCourseRequest;
import com.yfive.gbjs.domain.course.dto.response.CourseListResponse;
import com.yfive.gbjs.domain.course.dto.response.CourseResponse;
import com.yfive.gbjs.domain.course.entity.Course;
import com.yfive.gbjs.domain.course.entity.CourseSpot;
import com.yfive.gbjs.domain.course.entity.DailyCourse;
import com.yfive.gbjs.domain.course.exception.CourseErrorStatus;
import com.yfive.gbjs.domain.course.repository.CourseRepository;
import com.yfive.gbjs.domain.seal.entity.Seal;
import com.yfive.gbjs.domain.seal.exception.SealErrorStatus;
import com.yfive.gbjs.domain.seal.repository.SealRepository;
import com.yfive.gbjs.domain.user.entity.User;
import com.yfive.gbjs.domain.user.exception.UserErrorStatus;
import com.yfive.gbjs.domain.user.repository.UserRepository;
import com.yfive.gbjs.global.error.exception.EntityNotFoundException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 코스 서비스 구현체
 * 띠부씰 여행 코스 관련 비즈니스 로직을 구현
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

  private final CourseRepository courseRepository;
  private final SealRepository sealRepository;
  private final UserRepository userRepository;

  /**
   * 지역과 날짜를 기반으로 띠부씰 여행 코스를 자동 생성
   * 해당 지역의 띠부씰을 랜덤하게 배치하여 일별 코스를 구성
   */
  @Override
  @Transactional
  public CourseResponse createCourse(Long userId, CreateCourseRequest request) {
    // 사용자 확인
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException(UserErrorStatus.USER_NOT_FOUND));

    // 해당 지역의 띠부씰 필터링
    List<Seal> locationSeals = sealRepository.findAll().stream()
        .filter(seal -> seal.getLocation().equals(request.getLocation()))
        .collect(Collectors.toList());

    if (locationSeals.isEmpty()) {
      throw new EntityNotFoundException(SealErrorStatus.SEAL_NOT_FOUND);
    }

    // 여행 일수 및 일별 방문 장소 수 계산
    int totalDays = (int) ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
    int spotsPerDay = Math.max(1, locationSeals.size() / totalDays);

    // 코스 엔티티 생성
    Course course = Course.builder()
        .user(user)
        .title("임시 코스")
        .location(request.getLocation())
        .startDate(request.getStartDate())
        .endDate(request.getEndDate())
        .isSaved(false)
        .build();

    // 띠부씰을 랜덤하게 섮어서 코스 구성
    List<Seal> shuffledSeals = new ArrayList<>(locationSeals);
    Collections.shuffle(shuffledSeals);

    // 각 날짜별로 띠부씰 할당
    int sealIndex = 0;
    for (int day = 1; day <= totalDays; day++) {
      DailyCourse dailyCourse = DailyCourse.builder()
          .dayNumber(day)
          .build();
      course.addDailyCourse(dailyCourse);

      // 마지막 날은 남은 모든 띠부씰 할당
      int spotsForThisDay = (day == totalDays) 
          ? shuffledSeals.size() - sealIndex 
          : Math.min(spotsPerDay, shuffledSeals.size() - sealIndex);

      // 해당 날짜에 방문할 띠부씰 추가
      for (int spot = 0; spot < spotsForThisDay && sealIndex < shuffledSeals.size(); spot++) {
        CourseSpot courseSpot = CourseSpot.builder()
            .seal(shuffledSeals.get(sealIndex++))
            .order(spot + 1)
            .build();
        dailyCourse.addCourseSpot(courseSpot);
      }
    }

    Course savedCourse = courseRepository.save(course);
    return CourseResponse.of(savedCourse);
  }

  /**
   * 생성된 코스를 사용자가 제목을 지정하여 저장
   * 코스 소유자만 저장 가능
   */
  @Override
  @Transactional
  public CourseResponse saveCourse(Long userId, Long courseId, SaveCourseRequest request) {
    // 코스 조회
    Course course = courseRepository.findById(courseId)
        .orElseThrow(() -> new EntityNotFoundException(CourseErrorStatus.COURSE_NOT_FOUND));

    // 코스 소유자 확인
    if (!course.getUser().getId().equals(userId)) {
      throw new EntityNotFoundException(CourseErrorStatus.COURSE_NOT_FOUND);
    }

    // 코스 제목 및 저장 상태 업데이트
    course.updateTitle(request.getTitle());
    course.updateSaveStatus(true);
    Course savedCourse = courseRepository.save(course);
    
    return CourseResponse.of(savedCourse);
  }

  /**
   * 사용자가 저장한 모든 코스를 조회
   * 최신 순으로 정렬하여 반환
   */
  @Override
  public CourseListResponse getSavedCourses(Long userId) {
    List<Course> savedCourses = courseRepository.findSavedCoursesByUserId(userId);
    List<CourseResponse> courseResponses = savedCourses.stream()
        .map(CourseResponse::of)
        .collect(Collectors.toList());

    return CourseListResponse.of(courseResponses);
  }
}