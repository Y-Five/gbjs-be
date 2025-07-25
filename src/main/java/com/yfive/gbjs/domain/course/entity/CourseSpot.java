/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.entity;

import com.yfive.gbjs.domain.seal.entity.Seal;
import com.yfive.gbjs.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 코스 장소 엔티티
 * 일별 코스에 포함된 각 방문 장소(띠부씰) 정보를 관리
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "course_spot")
public class CourseSpot extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * 소속된 일별 코스
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "daily_course_id", nullable = false)
  private DailyCourse dailyCourse;

  /**
   * 방문 장소 (띠부씰)
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seal_id", nullable = false)
  private Seal seal;

  /**
   * 방문 순서
   */
  @Column(name = "spot_order", nullable = false)
  private Integer order;

  /**
   * 일별 코스 설정 (양방향 연관관계 설정용)
   *
   * @param dailyCourse 설정할 일별 코스
   */
  public void setDailyCourse(DailyCourse dailyCourse) {
    this.dailyCourse = dailyCourse;
  }
}