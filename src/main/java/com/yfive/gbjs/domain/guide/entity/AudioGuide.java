/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.yfive.gbjs.domain.tts.entity.AudioFile;
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

  @Builder.Default
  @OneToMany(mappedBy = "audioGuide", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<AudioFile> audioFiles = new ArrayList<>();

  @Column(name = "tid", nullable = false)
  private String tid;

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

  @Column(name = "content_id")
  private Long contentId;

  @Column(name = "sync_status", length = 1)
  private String syncStatus; // A: 신규, U: 수정, D: 삭제

  @Column(name = "api_modified_time")
  private String apiModifiedTime; // API에서 제공하는 수정시간

  @Column(name = "api_created_time")
  private String apiCreatedTime; // API에서 제공하는 생성시간

  @Column(name = "last_synced_at")
  private String lastSyncedAt; // 마지막 동기화 시간

  public void updateFromSync(AudioGuide updatedData) {
    // 모든 정보 업데이트 (syncList API도 오디오 정보를 포함함)
    this.tid = updatedData.getTid();
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
    this.audioTitle = updatedData.getAudioTitle();
    this.script = updatedData.getScript();
    this.playTime = updatedData.getPlayTime();
    this.audioUrl = updatedData.getAudioUrl();
    this.audioGuideId = updatedData.getAudioGuideId();
    this.stlid = updatedData.getStlid();
  }

  public void addAudioFile(AudioFile audioFile) {
    this.audioFiles.add(audioFile);
  }
}
