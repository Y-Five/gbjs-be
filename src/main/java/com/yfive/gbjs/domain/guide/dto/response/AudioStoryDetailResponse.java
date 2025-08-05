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
@Schema(title = "AudioStoryDetailResponse DTO", description = "오디오 스토리 상세 정보 응답")
public class AudioStoryDetailResponse {

  @Schema(description = "오디오 스토리 상세 목록")
  private List<AudioStoryDetail> audioSpotList;

  @Getter
  @Builder
  @Schema(title = "AudioStoryDetail", description = "오디오 스토리 상세 정보")
  public static class AudioStoryDetail {

    @Schema(description = "관광지 ID", example = "TH00001")
    private String spotId;

    @Schema(description = "오디오 스토리 ID", example = "ST00001")
    private String audioStoryId;

    @Schema(description = "관광지명", example = "경복궁")
    private String title;

    @Schema(description = "경도", example = "126.977041")
    private String addr1;

    @Schema(description = "위도", example = "37.579617")
    private String addr2;

    @Schema(description = "오디오 제목", example = "경복궁의 역사")
    private String audioTitle;

    @Schema(description = "대본", example = "경복궁은 조선 왕조의 법궁으로...")
    private String script;

    @Schema(description = "재생 시간(초)", example = "240")
    private Integer playTime;

    @Schema(description = "오디오 파일 URL", example = "https://example.com/story_audio.mp3")
    private String audioUrl;

    @Schema(description = "관광지 이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;
  }
}
