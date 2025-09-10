/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.yfive.gbjs.domain.guide.entity.AudioGuide;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "audio")
public class AudioFile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(nullable = false)
  private String type;

  @Column(name = "fileUrl", nullable = false)
  private String fileUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "audio_guide_id")
  private AudioGuide audioGuide;

  public void updateUrl(String fileUrl) {
    this.fileUrl = fileUrl;
  }
}
