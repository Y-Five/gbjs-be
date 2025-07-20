/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "AudioStoryListResponse DTO", description = "오디오 스토리 목록 응답")
public class AudioStoryListResponse {

  @Schema(description = "전체 결과 수", example = "50")
  private Integer totalCount;

  @Schema(description = "페이지 번호", example = "1")
  private Integer pageNo;

  @Schema(description = "페이지당 결과 수", example = "10")
  private Integer numOfRows;

  @Schema(description = "오디오 스토리 목록")
  private List<AudioStorySpot> audioSpotList;

  @Getter
  @Builder
  @Schema(title = "AudioStoryItem", description = "오디오 스토리 항목")
  public static class AudioStorySpot {

    @Schema(description = "오디오 스토리 ID", example = "ST00001")
    private String audioStoryId;

    @Schema(description = "관광지 ID", example = "TH00001")
    private String spotId;

    @Schema(description = "오디오 스토리 제목", example = "경복궁의 역사")
    private String title;

    @Schema(description = "오디오 스토리 내용", example = "경복궁은 조선 왕조의 법궁으로...")
    private String content;

    @Schema(description = "카테고리", example = "역사")
    private String category;

    @Schema(description = "오디오 파일 URL", example = "https://example.com/story_audio.mp3")
    private String audioUrl;

    @Schema(description = "재생 시간(초)", example = "240")
    private Integer playTime;

    @Schema(description = "언어", example = "ko")
    private String language;

    @Schema(description = "거리(m) - 위치기반 조회시만", example = "300")
    private Integer distance;

    @Schema(description = "위도", example = "37.579617")
    private Double latitude;

    @Schema(description = "경도", example = "126.977041")
    private Double longitude;
  }
}
