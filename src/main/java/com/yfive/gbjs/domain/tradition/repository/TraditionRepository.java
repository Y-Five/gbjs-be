/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tradition.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yfive.gbjs.domain.tradition.entity.Tradition;
import com.yfive.gbjs.domain.tradition.entity.TraditionType;

@Repository
public interface TraditionRepository extends JpaRepository<Tradition, Long> {

  Page<Tradition> findByType(TraditionType type, Pageable pageable);
}
