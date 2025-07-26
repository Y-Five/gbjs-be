package com.yfive.gbjs.domain.seal.entity.mapper;

import java.io.Serializable;

import jakarta.persistence.*;

import com.yfive.gbjs.domain.seal.entity.Seal;
import com.yfive.gbjs.domain.user.entity.User;
import com.yfive.gbjs.global.common.entity.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자가 수집한 띠부씰 정보를 관리하는 엔티티 사용자와 띠부씰 간의 다대다 관계를 매핑 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "user_seal")
@IdClass(UserSeal.UserSealId.class)
public class UserSeal extends BaseTimeEntity {

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seal_id", nullable = false)
  private Seal seal;

  @Column(name = "collected_at", nullable = false)
  private java.time.LocalDateTime collectedAt;

  /** 복합키 클래스 */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @EqualsAndHashCode
  public static class UserSealId implements Serializable {
    private Long user;
    private Long seal;
  }
}
