/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.entity;

import jakarta.persistence.*;

import com.yfive.gbjs.domain.guide.entity.AudioGuide;
import com.yfive.gbjs.global.common.entity.BaseTimeEntity;

import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "seal_spot")
public class SealSpot extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "location", nullable = false)
  private Location location;

  @Column(name = "address")
  private String address;

  @Column(name = "image_url")
  private String imageUrl;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "audio_guide_id")
  private AudioGuide audioGuide;

  @Enumerated(EnumType.STRING)
  @Column(name = "category")
  private SealSpotCategory category;
}
