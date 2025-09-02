/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.entity;

import jakarta.persistence.*;

import com.yfive.gbjs.global.common.entity.BaseTimeEntity;

import lombok.*;

/** 띠부씰 엔티티 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "seal")
public class Seal extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "number", nullable = false, unique = true)
  private Integer number;

  @Column(name = "spotName", nullable = false, unique = true)
  private String spotName;

  @Column(name = "locationName", nullable = false)
  private String locationName;

  @Column(name = "content", nullable = false)
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(name = "rarity", nullable = false)
  private Rarity rarity;

  @Column(name = "frontImageUrl", nullable = false)
  private String frontImageUrl;

  @Column(name = "backImageUrl", nullable = false)
  private String backImageUrl;

  // 외래키 관계
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seal_spot_id")
  private SealSpot sealSpot;

  @Enumerated(EnumType.STRING)
  @Column(name = "location", nullable = false)
  private Location location;
}
