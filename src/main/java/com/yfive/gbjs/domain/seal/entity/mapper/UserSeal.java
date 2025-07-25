/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.entity.mapper;

import com.yfive.gbjs.domain.seal.entity.Seal;
import com.yfive.gbjs.domain.seal.entity.UserSealId;
import com.yfive.gbjs.domain.user.entity.User;
import com.yfive.gbjs.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자가 수집한 띠부씰 정보를 관리하는 엔티티
 * 사용자와 띠부씰 간의 다대다 관계를 매핑
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "user_seal")
public class UserSeal extends BaseTimeEntity {

  /**
   * 복합키 (userId, sealId)
   */
  @EmbeddedId
  private UserSealId id;

  /**
   * 사용자 정보
   */
  @MapsId("userId")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  /**
   * 띠부씰 정보
   */
  @MapsId("sealId")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seal_id", nullable = false)  
  private Seal seal;

  /**
   * 수집 일시
   */
  @Column(name = "collected_at", nullable = false)
  private java.time.LocalDateTime collectedAt;
}