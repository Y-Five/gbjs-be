/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.repository;

import com.yfive.gbjs.domain.course.entity.Course;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 코스 리포지토리
 * 코스 엔티티에 대한 데이터베이스 접근을 담당
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

  /**
   * 특정 사용자가 저장한 코스 목록 조회
   *
   * @param userId 사용자 ID
   * @return 저장된 코스 목록 (최신순 정렬)
   */
  @Query("SELECT c FROM Course c WHERE c.user.id = :userId AND c.isSaved = true ORDER BY c.createdAt DESC")
  List<Course> findSavedCoursesByUserId(@Param("userId") Long userId);

  /**
   * 특정 사용자의 모든 코스 조회
   *
   * @param userId 사용자 ID
   * @return 모든 코스 목록 (최신순 정렬)
   */
  @Query("SELECT c FROM Course c WHERE c.user.id = :userId ORDER BY c.createdAt DESC")
  List<Course> findByUserId(@Param("userId") Long userId);
}