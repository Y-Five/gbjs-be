/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yfive.gbjs.domain.course.entity.mapper.DailyCourseSpot;

public interface DailyCourseSpotRespository extends JpaRepository<DailyCourseSpot, Long> {}
