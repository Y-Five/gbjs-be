/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import com.yfive.gbjs.domain.user.entity.User;
import com.yfive.gbjs.global.common.entity.BaseTimeEntity;

import lombok.*;

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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  // Fields for recommendations
  @Enumerated(EnumType.STRING)
  @Column(name = "recommendation_type")
  private RecommendationType recommendationType;

  @Column(name = "thumbnail_image_url")
  private String thumbnailImageUrl;

  @Column(name = "location_name")
  private String locationName;

  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("dayNumber ASC")
  @Builder.Default
  private List<DailyCourse> dailyCourses = new ArrayList<>();

  public void addDailyCourse(DailyCourse dailyCourse) {
    this.dailyCourses.add(dailyCourse);
    dailyCourse.setCourse(this);
  }
}
