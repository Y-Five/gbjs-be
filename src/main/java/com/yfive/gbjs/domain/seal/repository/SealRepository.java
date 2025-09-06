/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yfive.gbjs.domain.seal.entity.Seal;

/** 띠부씰 리포지토리 띠부씰 엔티티에 대한 데이터베이스 접근을 담당 */
@Repository
public interface SealRepository extends JpaRepository<Seal, Long> {
  Optional<Seal> findBySealSpotId(Long sealSpotId);

  List<Seal> findAllByLocationNameIn(List<String> locationNames);

  List<Seal> findBySealSpot_Id(Long sealSpotId);
}
