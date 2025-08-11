/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yfive.gbjs.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);

  Optional<User> findByNickname(String nickname);
}
