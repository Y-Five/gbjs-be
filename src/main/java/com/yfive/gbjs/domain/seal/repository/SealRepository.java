/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.repository;

import com.yfive.gbjs.domain.seal.entity.Seal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 띠부씰 리포지토리
 * 띠부씰 엔티티에 대한 데이터베이스 접근을 담당
 */
@Repository
public interface SealRepository extends JpaRepository<Seal, Long> {
}