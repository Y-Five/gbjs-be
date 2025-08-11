/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yfive.gbjs.domain.seal.entity.SealProduct;

@Repository
public interface SealProductRepository extends JpaRepository<SealProduct, Long> {}
