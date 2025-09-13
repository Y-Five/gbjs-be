/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yfive.gbjs.domain.course.entity.RecommendCourse;
import com.yfive.gbjs.domain.course.entity.RecommendationType;

@Repository
public interface RecommendCourseRepository extends JpaRepository<RecommendCourse, Long> {

  List<RecommendCourse> findTop4ByType(RecommendationType type);
}
