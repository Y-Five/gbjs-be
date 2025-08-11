/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import com.yfive.gbjs.domain.course.entity.mapper.DailyCourseSpot;
import com.yfive.gbjs.domain.seal.entity.Location;
import com.yfive.gbjs.global.common.entity.BaseTimeEntity;

import lombok.*;

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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false)
  @Setter
  private Course course;

  @Column(name = "day_number", nullable = false)
  private Integer dayNumber;

  @Column(name = "date", nullable = false)
  private LocalDate date;

  @Enumerated(EnumType.STRING)
  @Column(name = "location", nullable = false)
  private Location location;

  @OneToMany(mappedBy = "dailyCourse", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("visitOrder ASC")
  @Builder.Default
  private List<DailyCourseSpot> spots = new ArrayList<>();

  public void addSpot(DailyCourseSpot spot) {
    this.spots.add(spot);
    spot.setDailyCourse(this);
  }
}
