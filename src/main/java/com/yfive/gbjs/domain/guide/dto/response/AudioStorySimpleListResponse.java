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
@Schema(title = "AudioStorySimpleListResponse DTO", description = "오디오 스토리 간략 목록 응답")
public class AudioStorySimpleListResponse {

  @Schema(description = "전체 결과 수", example = "50")
  private Integer totalCount;

  @Schema(description = "페이지 번호", example = "1")
  private Integer pageNo;

  @Schema(description = "페이지당 결과 수", example = "10")
  private Integer pageSize;

  @Schema(description = "첫 페이지 여부", example = "true")
  private Boolean first;

  @Schema(description = "마지막 페이지 여부", example = "false")
  private Boolean last;

  @Schema(description = "오디오 스토리 간략 목록")
  private List<AudioStorySimpleSpot> audioSpotList;

  @Getter
  @Builder
  @Schema(title = "AudioStorySimpleSpot", description = "오디오 스토리 간략 정보")
  public static class AudioStorySimpleSpot {

    @Schema(description = "관광지 ID", example = "TH00001")
    private String spotId;

    @Schema(description = "관광지명", example = "경복궁")
    private String title;

    @Schema(description = "경도", example = "126.977041")
    private String addr1;

    @Schema(description = "위도", example = "37.579617")
    private String addr2;

    @Schema(description = "오디오 파일 URL", example = "https://example.com/story_audio.mp3")
    private String audioUrl;

    @Schema(description = "관광지 이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;
  }
}
