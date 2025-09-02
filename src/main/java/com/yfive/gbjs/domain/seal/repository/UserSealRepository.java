/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yfive.gbjs.domain.seal.entity.mapper.UserSeal;

/** 사용자 띠부씰 수집 정보 리포지토리 사용자와 띠부씰 간의 매핑 정보를 관리 */
@Repository
public interface UserSealRepository extends JpaRepository<UserSeal, Long> {

  /**
   * 특정 사용자가 수집한 모든 띠부씰 조회
   *
   * @param userId 사용자 ID
   * @return 사용자가 수집한 띠부씰 목록
   */
  @Query("SELECT us FROM UserSeal us WHERE us.user.id = :userId")
  List<UserSeal> findByUserId(@Param("userId") Long userId);

  /**
   * 특정 사용자가 수집한 띠부씰 개수 조회
   *
   * @param userId 사용자 ID
   * @return 사용자가 수집한 띠부씰 개수
   */
  @Query("SELECT COUNT(us) FROM UserSeal us WHERE us.user.id = :userId")
  long countByUserId(@Param("userId") Long userId);

  /**
   * 특정 사용자가 특정 띠부씰을 수집했는지 확인
   *
   * @param userId 사용자 ID
   * @param sealId 띠부씰 ID
   * @return 수집 여부
   */
  @Query(
      "SELECT COUNT(us) > 0 FROM UserSeal us WHERE us.user.id = :userId AND us.seal.id = :sealId")
  boolean existsByUserIdAndSealId(@Param("userId") Long userId, @Param("sealId") Long sealId);

  /**
   * 특정 사용자가 특정 띠부씰을 수집했는지 확인 (JPA 명명 규칙)
   *
   * @param userId 사용자 ID
   * @param sealId 띠부씰 ID
   * @return 수집 여부
   */
  boolean existsByUser_IdAndSeal_Id(Long userId, Long sealId);

  /**
   * 특정 사용자의 특정 띠부씰 수집 정보 조회(띠부씰 삭제시 사용)
   *
   * @param userId 사용자 ID
   * @param sealId 띠부씰 ID
   * @return UserSeal 엔티티
   */
  Optional<UserSeal> findByUser_IdAndSeal_Id(Long userId, Long sealId);

  /**
   * 여러 사용자의 띠부씰 수를 한 번의 쿼리로 조회합니다.
   *
   * @param userIds 사용자 ID 리스트
   * @return 키가 사용자 ID이고 값이 해당 사용자의 띠부씰 수인 Map 객체
   */
  @Query(
      "SELECT us.user.id, COUNT(us) FROM UserSeal us WHERE us.user.id IN :userIds GROUP BY us.user.id")
  Map<Long, Long> countSealsByUserIds(@Param("userIds") List<Long> userIds);
}
