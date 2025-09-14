/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.repository;

import com.yfive.gbjs.domain.course.entity.UserCourse;
import com.yfive.gbjs.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCourseRepository extends JpaRepository<UserCourse, Long> {

    List<UserCourse> findByUser(User user);

    java.util.Optional<UserCourse> findByUserIdAndCourseId(Long userId, Long courseId);

}
