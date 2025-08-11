/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yfive.gbjs.domain.tts.entity.AudioFile;

@Repository
public interface TtsRepository extends JpaRepository<AudioFile, Long> {}
