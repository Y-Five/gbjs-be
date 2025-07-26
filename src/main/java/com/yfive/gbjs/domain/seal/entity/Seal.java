package com.yfive.gbjs.domain.seal.entity;

import jakarta.persistence.*;

import com.yfive.gbjs.global.common.entity.BaseTimeEntity;

import lombok.*;

/** 띠부씰 엔티티 */
@Entity
@Getter
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

  @Column(name = "locationName", nullable = false, unique = true)
  private String locationName;

  @Enumerated(EnumType.STRING)
  @Column(name = "location", nullable = false)
  private Location location;

  @Column(name = "content", nullable = false)
  private String content;

  @Column(name = "imageUrl", nullable = false)
  private String imageUrl;
}
