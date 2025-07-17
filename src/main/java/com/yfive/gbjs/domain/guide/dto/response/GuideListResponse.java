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
@Schema(title = "GuideListResponse DTO", description = "관광지 목록 응답")
public class GuideListResponse {

  @Schema(description = "전체 결과 수", example = "100")
  private Integer totalCount;

  @Schema(description = "페이지 번호", example = "1")
  private Integer pageNo;

  @Schema(description = "페이지당 결과 수", example = "10")
  private Integer numOfRows;

  @Schema(description = "관광지 목록")
  private List<GuideItem> items;

  @Getter
  @Builder
  @Schema(title = "GuideItem", description = "관광지 항목")
  public static class GuideItem {

    @Schema(description = "관광지 ID", example = "TH00001")
    private String themeId;

    @Schema(description = "관광지명", example = "경복궁")
    private String title;

    @Schema(description = "주소", example = "서울특별시 종로구 사직로 161")
    private String address;

    @Schema(description = "설명", example = "조선시대 왕궁")
    private String description;

    @Schema(description = "위도", example = "37.579617")
    private Double latitude;

    @Schema(description = "경도", example = "126.977041")
    private Double longitude;

    @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "오디오 파일 URL", example = "https://example.com/audio.mp3")
    private String audioUrl;

    @Schema(description = "재생 시간(초)", example = "180")
    private Integer playTime;

    @Schema(description = "거리(m) - 위치기반 조회시만", example = "500")
    private Integer distance;
  }
}
