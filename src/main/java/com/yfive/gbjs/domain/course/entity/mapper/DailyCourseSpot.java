/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.entity.mapper;

import jakarta.persistence.*;

import com.yfive.gbjs.domain.course.entity.DailyCourse;
import com.yfive.gbjs.domain.seal.entity.SealSpot;
import com.yfive.gbjs.global.common.entity.BaseTimeEntity;

import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "daily_course_spot")
public class DailyCourseSpot extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "daily_course_id", nullable = false)
  @Setter
  private DailyCourse dailyCourse;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "spot_id", nullable = false)
  private SealSpot sealSpot;

  @Column(name = "visit_order", nullable = false)
  private Integer visitOrder;
}
