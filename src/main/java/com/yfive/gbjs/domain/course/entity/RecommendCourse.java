/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.entity;

import jakarta.persistence.*;

import com.yfive.gbjs.global.common.entity.BaseTimeEntity;

import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "recommend_course")
public class RecommendCourse extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "image_url")
  private String imageUrl;

  @Column(name = "location_name")
  private String locationName;

  @Enumerated(EnumType.STRING)
  @Column(name = "type")
  private RecommendationType type;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;
}
