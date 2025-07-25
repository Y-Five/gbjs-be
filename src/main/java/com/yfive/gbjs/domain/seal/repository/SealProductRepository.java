/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.repository;

import com.yfive.gbjs.domain.seal.entity.SealProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SealProductRepository extends JpaRepository<SealProduct, Long> {
}