/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yfive.gbjs.domain.course.entity.UserCourse;
import com.yfive.gbjs.domain.user.entity.User;

@Repository
public interface UserCourseRepository extends JpaRepository<UserCourse, Long> {

  List<UserCourse> findByUser(User user);

  java.util.Optional<UserCourse> findByUserIdAndCourseId(Long userId, Long courseId);
}
