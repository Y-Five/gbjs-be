/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yfive.gbjs.domain.course.entity.DailyCourse;

public interface DailyCourseRepository extends JpaRepository<DailyCourse, Long> {}
