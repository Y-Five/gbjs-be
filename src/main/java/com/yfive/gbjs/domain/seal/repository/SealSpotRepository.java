/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yfive.gbjs.domain.seal.entity.Location;
import com.yfive.gbjs.domain.seal.entity.SealSpot;

@Repository
public interface SealSpotRepository extends JpaRepository<SealSpot, Long> {
  List<SealSpot> findByLocation(Location location);
}
