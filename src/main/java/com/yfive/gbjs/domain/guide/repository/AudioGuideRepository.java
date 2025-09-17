/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yfive.gbjs.domain.guide.entity.AudioGuide;

@Repository
public interface AudioGuideRepository extends JpaRepository<AudioGuide, Long> {

  // tid로 조회
  Optional<AudioGuide> findByTid(String tid);

  List<AudioGuide> findByContentId(Long contentId);

  boolean existsByContentId(Long contentId);

  // tid로 삭제
  void deleteByTid(String tid);

  // 마지막 동기화 시간 조회
  @Query("SELECT MAX(a.apiModifiedTime) FROM AudioGuide a")
  Optional<String> findLatestModifiedTime();

  // 관광지명으로 조회 (정확히 일치)
  List<AudioGuide> findByTitle(String title);

  // 관광지명으로 조회 (LIKE 검색 - 부분 일치)
  @Query("SELECT a FROM AudioGuide a WHERE a.title LIKE CONCAT('%', :title, '%')")
  List<AudioGuide> findByTitleLike(@Param("title") String title);

  // 주어진 contentId 목록 중 오디오 가이드가 존재하는 contentId들을 조회
  @Query("SELECT DISTINCT ag.contentId FROM AudioGuide ag WHERE ag.contentId IN :contentIds")
  List<Long> findContentIdsWithAudioGuidesByContentIdIn(@Param("contentIds") List<Long> contentIds);
}
