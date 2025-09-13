/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.converter;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.yfive.gbjs.domain.course.dto.response.CourseResponse;
import com.yfive.gbjs.domain.course.entity.*;
import com.yfive.gbjs.domain.course.entity.mapper.DailyCourseSpot;
import com.yfive.gbjs.domain.course.exception.CourseErrorStatus;
import com.yfive.gbjs.domain.seal.entity.Location;
import com.yfive.gbjs.domain.seal.entity.SealSpot;
import com.yfive.gbjs.domain.seal.entity.SealSpotCategory;
import com.yfive.gbjs.global.error.exception.CustomException;

/** 여행 코스 관련 데이터 변환을 담당하는 컨버터 Entity ↔ DTO 변환 및 데이터 포맷팅 처리 */
@Component
public class CourseConverter {

  /** Course 엔티티를 상세 응답 DTO로 변환합니다. */
  public CourseResponse.CourseDetailDTO toCourseDetailDTO(Course course) {
    return CourseResponse.CourseDetailDTO.builder()
        .id(course.getId())
        .title(course.getTitle())
        .startDate(course.getStartDate())
        .endDate(course.getEndDate())
        .dailyCourses(
            course.getDailyCourses().stream()
                .map(this::toDailyCourseDTO)
                .collect(Collectors.toList()))
        .build();
  }

  /** DailyCourse 엔티티를 일차별 응답 DTO로 변환합니다. */
  public CourseResponse.DailyCourseDTO toDailyCourseDTO(DailyCourse dailyCourse) {
    return CourseResponse.DailyCourseDTO.builder()
        .dayNumber(dailyCourse.getDayNumber())
        .date(dailyCourse.getDate())
        .location(getLocationKoreanName(dailyCourse.getLocation()))
        .spots(
            dailyCourse.getSpots().stream().map(this::toSimpleSpotDTO).collect(Collectors.toList()))
        .build();
  }

  /** DailyCourseSpot 엔티티를 관광지 응답 DTO로 변환합니다. */
  public CourseResponse.SpotDTO toSpotDTO(DailyCourseSpot dailyCourseSpot) {
    SealSpot sealSpot = dailyCourseSpot.getSealSpot();
    return CourseResponse.SpotDTO.builder()
        .spotId(sealSpot.getId())
        .visitOrder(dailyCourseSpot.getVisitOrder())
        .name(sealSpot.getName())
        .description(sealSpot.getDescription())
        .address(sealSpot.getAddr1())
        .category(
            sealSpot.getCategory() != null ? getCategoryKoreanName(sealSpot.getCategory()) : null)
        .imageUrl(sealSpot.getImageUrl())
        .build();
  }

  /** DailyCourseSpot 엔티티를 간략한 관광지 응답 DTO로 변환합니다. */
  public CourseResponse.SimpleSpotDTO toSimpleSpotDTO(DailyCourseSpot dailyCourseSpot) {
    SealSpot sealSpot = dailyCourseSpot.getSealSpot();
    return CourseResponse.SimpleSpotDTO.builder()
        .spotId(dailyCourseSpot.getSealSpot().getSpotId())
        .visitOrder(dailyCourseSpot.getVisitOrder())
        .name(sealSpot.getName())
        .category(
            sealSpot.getCategory() != null ? getCategoryKoreanName(sealSpot.getCategory()) : null)
        .addr1(sealSpot.getAddr1())
        .latitude(sealSpot.getLatitude())
        .longitude(sealSpot.getLongitude())
        .isSealSpot(true)
        .sealSpotId(sealSpot.getId())
        .build();
  }

  /** Course 엔티티를 요약 응답 DTO로 변환합니다. 목록 조회 시 사용 (상세 정보 제외) */
  public CourseResponse.CourseSummaryDTO toCourseSummaryDTO(
      Course course, int totalCollectableSeals, int userCollectedSeals) {
    long totalDays = ChronoUnit.DAYS.between(course.getStartDate(), course.getEndDate()) + 1;

    List<String> locations =
        course.getDailyCourses().stream()
            .map(day -> getLocationKoreanName(day.getLocation()))
            .distinct()
            .collect(Collectors.toList());

    return CourseResponse.CourseSummaryDTO.builder()
        .id(course.getId())
        .title(course.getTitle())
        .startDate(course.getStartDate())
        .endDate(course.getEndDate())
        .totalDays((int) totalDays)
        .locations(locations)
        .totalCollectableSeals(totalCollectableSeals)
        .userCollectedSeals(userCollectedSeals)
        .build();
  }

  /** RecommendCourse 엔티티를 응답 DTO로 변환합니다. */
  public CourseResponse.RecommendedCourseDTO toRecommendedCourseDTO(
      RecommendCourse recommendCourse) {
    return CourseResponse.RecommendedCourseDTO.builder()
        .courseId(recommendCourse.getCourse().getId())
        .title(recommendCourse.getTitle())
        .locationName(recommendCourse.getLocationName())
        .image(recommendCourse.getImageUrl())
        .build();
  }

  /**
   * Location enum을 한글 지역명으로 변환합니다.
   *
   * @param location Location enum 값
   * @return 한글 지역명 (예: GYEONGJU → "경주시")
   */
  public String getLocationKoreanName(Location location) {
    return switch (location) {
      case GYEONGSAN -> "경산시";
      case GYEONGJU -> "경주시";
      case GORYEONG -> "고령군";
      case GUMI -> "구미시";
      case GUNWI -> "군위군";
      case GIMCHEON -> "김천시";
      case MUNGYEONG -> "문경시";
      case BONGHWA -> "봉화군";
      case SANGJU -> "상주시";
      case SEONGJU -> "성주군";
      case ANDONG -> "안동시";
      case YEONGDEOK -> "영덕군";
      case YEONGYANG -> "영양군";
      case YEONGJU -> "영주시";
      case YEONGCHEON -> "영천시";
      case YECHEON -> "예천군";
      case ULLUNG -> "울릉군";
      case ULJIN -> "울진군";
      case UISEONG -> "의성군";
      case CHEONGDO -> "청도군";
      case CHEONGSONG -> "청송군";
      case CHILGOK -> "칠곡군";
      case POHANG -> "포항시";
      default -> location.name();
    };
  }

  /**
   * 한글 지역명을 Location enum으로 변환합니다.
   *
   * @param koreanName 한글 지역명 (예: "경주시")
   * @return Location enum 값
   */
  public Location getLocationFromKoreanName(String koreanName) {
    return switch (koreanName) {
      case "경산시" -> Location.GYEONGSAN;
      case "경주시" -> Location.GYEONGJU;
      case "고령군" -> Location.GORYEONG;
      case "구미시" -> Location.GUMI;
      case "군위군" -> Location.GUNWI;
      case "김천시" -> Location.GIMCHEON;
      case "문경시" -> Location.MUNGYEONG;
      case "봉화군" -> Location.BONGHWA;
      case "상주시" -> Location.SANGJU;
      case "성주군" -> Location.SEONGJU;
      case "안동시" -> Location.ANDONG;
      case "영덕군" -> Location.YEONGDEOK;
      case "영양군" -> Location.YEONGYANG;
      case "영주시" -> Location.YEONGJU;
      case "영천시" -> Location.YEONGCHEON;
      case "예천군" -> Location.YECHEON;
      case "울릉군" -> Location.ULLUNG;
      case "울진군" -> Location.ULJIN;
      case "의성군" -> Location.UISEONG;
      case "청도군" -> Location.CHEONGDO;
      case "청송군" -> Location.CHEONGSONG;
      case "칠곡군" -> Location.CHILGOK;
      case "포항시" -> Location.POHANG;
      default -> throw new CustomException(CourseErrorStatus._INVALID_LOCATION);
    };
  }

  /**
   * SealSpotCategory enum을 한글 카테고리명으로 변환합니다.
   *
   * @param category SealSpotCategory enum 값
   * @return 한글 카테고리명 (예: NATURE → "자연환경")
   */
  public String getCategoryKoreanName(SealSpotCategory category) {
    return switch (category) {
      case NATURE -> "자연환경";
      case NIGHTSCAPE -> "야경 명소";
      case HEALING -> "힐링 명소";
      case ATTRACTION -> "유명 관광지";
      case ACTIVITY -> "액티비티";
    };
  }
}
