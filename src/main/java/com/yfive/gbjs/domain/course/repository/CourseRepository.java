/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yfive.gbjs.domain.course.entity.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {}
