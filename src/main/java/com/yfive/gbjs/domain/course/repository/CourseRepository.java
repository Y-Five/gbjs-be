/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yfive.gbjs.domain.course.entity.Course;
import com.yfive.gbjs.domain.user.entity.User;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
  @Query(
      "SELECT c FROM Course c JOIN FETCH c.dailyCourses dc WHERE c.user = :user ORDER BY c.startDate DESC")
  List<Course> findByUserOrderByStartDateDesc(@Param("user") User user);

  boolean existsByIdAndUserId(Long courseId, Long userId);
}
