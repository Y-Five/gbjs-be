/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.entity;

import jakarta.persistence.*;

import com.yfive.gbjs.global.common.entity.BaseTimeEntity;

import lombok.*;

/** 띠부씰 상품 엔티티 띠부씰과 관련된 기념품/상품 정보를 관리 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "seal_product")
public class SealProduct extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "price", nullable = false)
  private String price;

  @Column(name = "image_url")
  private String imageUrl;
}
