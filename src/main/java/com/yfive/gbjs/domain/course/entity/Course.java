/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.entity;

import com.yfive.gbjs.domain.seal.entity.Location;
import com.yfive.gbjs.domain.user.entity.User;
import com.yfive.gbjs.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 띠부씰 여행 코스 엔티티
 * 사용자가 생성한 여행 코스 정보를 관리
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "course")
public class Course extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * 코스를 생성한 사용자
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  /**
   * 코스 제목
   */
  @Column(name = "title", nullable = false)
  private String title;

  /**
   * 여행 지역
   */
  @Column(name = "location", nullable = false)
  @Enumerated(EnumType.STRING)
  private Location location;

  /**
   * 여행 시작 날짜
   */
  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  /**
   * 여행 종료 날짜
   */
  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  /**
   * 저장 여부
   */
  @Column(name = "is_saved", nullable = false)
  @Builder.Default
  private boolean isSaved = false;

  /**
   * 일별 코스 목록
   */
  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<DailyCourse> dailyCourses = new ArrayList<>();

  /**
   * 일별 코스 추가
   *
   * @param dailyCourse 추가할 일별 코스
   */
  public void addDailyCourse(DailyCourse dailyCourse) {
    this.dailyCourses.add(dailyCourse);
    dailyCourse.setCourse(this);
  }

  /**
   * 저장 상태 업데이트
   *
   * @param isSaved 저장 여부
   */
  public void updateSaveStatus(boolean isSaved) {
    this.isSaved = isSaved;
  }

  /**
   * 코스 제목 업데이트
   *
   * @param title 새로운 제목
   */
  public void updateTitle(String title) {
    this.title = title;
  }
}