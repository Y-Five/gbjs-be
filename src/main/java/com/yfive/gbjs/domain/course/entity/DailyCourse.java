/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.entity;

import com.yfive.gbjs.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 일별 코스 엔티티
 * 여행 코스의 각 날짜별 세부 일정을 관리
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "daily_course")
public class DailyCourse extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * 소속된 코스
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  /**
   * 날짜 번호 (1일차, 2일차 등)
   */
  @Column(name = "day_number", nullable = false)
  private Integer dayNumber;

  /**
   * 해당 날짜의 방문 장소 목록
   */
  @OneToMany(mappedBy = "dailyCourse", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("order ASC")
  @Builder.Default
  private List<CourseSpot> courseSpots = new ArrayList<>();

  /**
   * 코스 설정 (양방향 연관관계 설정용)
   *
   * @param course 설정할 코스
   */
  public void setCourse(Course course) {
    this.course = course;
  }

  /**
   * 코스 장소 추가
   *
   * @param courseSpot 추가할 코스 장소
   */
  public void addCourseSpot(CourseSpot courseSpot) {
    this.courseSpots.add(courseSpot);
    courseSpot.setDailyCourse(this);
  }
}