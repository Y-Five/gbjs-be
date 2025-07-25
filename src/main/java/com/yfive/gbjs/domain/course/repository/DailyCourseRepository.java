/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.repository;

import com.yfive.gbjs.domain.course.entity.DailyCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 일별 코스 리포지토리
 * 일별 코스 엔티티에 대한 데이터베이스 접근을 담당
 */
@Repository
public interface DailyCourseRepository extends JpaRepository<DailyCourse, Long> {
}