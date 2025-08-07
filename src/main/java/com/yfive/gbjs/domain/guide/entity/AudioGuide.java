/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.entity;

import jakarta.persistence.*;

import com.yfive.gbjs.global.common.entity.BaseTimeEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audio_guide")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioGuide extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "spot_id", nullable = false)
  private String spotId;

  @Column(name = "tlid")
  private String tlid;

  @Column(name = "audio_guide_id")
  private String audioGuideId;

  @Column(name = "stlid")
  private String stlid;

  @Column(nullable = false)
  private String title;

  @Column(name = "longitude")
  private String longitude;

  @Column(name = "latitude")
  private String latitude;

  @Column(name = "audio_title")
  private String audioTitle;

  @Column(columnDefinition = "TEXT")
  private String script;

  @Column(name = "play_time")
  private Integer playTime;

  @Column(name = "audio_url")
  private String audioUrl;

  @Column(name = "lang_code")
  private String langCode;

  @Column(name = "image_url")
  private String imageUrl;

  @Column(name = "sync_status", length = 1)
  private String syncStatus; // A: 신규, U: 수정, D: 삭제

  @Column(name = "api_modified_time")
  private String apiModifiedTime; // API에서 제공하는 수정시간

  @Column(name = "api_created_time")
  private String apiCreatedTime; // API에서 제공하는 생성시간

  @Column(name = "last_synced_at")
  private String lastSyncedAt; // 마지막 동기화 시간

  public void updateFromSync(AudioGuide updatedData) {
    // 기본 정보만 업데이트 (syncList API는 오디오 정보를 포함하지 않음)
    this.title = updatedData.getTitle();
    this.longitude = updatedData.getLongitude();
    this.latitude = updatedData.getLatitude();
    this.imageUrl = updatedData.getImageUrl();
    this.syncStatus = updatedData.getSyncStatus();
    this.apiModifiedTime = updatedData.getApiModifiedTime();
    this.lastSyncedAt = updatedData.getLastSyncedAt();
    this.tlid = updatedData.getTlid();
    this.langCode = updatedData.getLangCode();
    this.apiCreatedTime = updatedData.getApiCreatedTime();
    // 오디오 관련 필드는 기존 값 유지
    // this.audioTitle, this.script, this.playTime, this.audioUrl
    // this.audioGuideId, this.stlid
  }
}
