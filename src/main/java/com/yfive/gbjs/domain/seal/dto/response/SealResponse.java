/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.dto.response;

import java.util.List;

import com.yfive.gbjs.domain.seal.entity.Location;
import com.yfive.gbjs.domain.seal.entity.Rarity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class SealResponse {

  @Builder
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(title = "Seal : 띠부씰 DTO")
  public static class SealDTO {
    @Schema(description = "띠부씰 ID", example = "1")
    private Long id;

    @Schema(description = "띠부씰 번호", example = "1")
    private Integer number;

    @Schema(description = "명소 이름", example = "하회마을")
    private String spotName;

    @Schema(description = "지역 이름", example = "안동")
    private String locationName;

    @Schema(description = "지역 코드", example = "ANDONG")
    private Location location;

    @Schema(description = "설명", example = "안동 하회별신굿탈놀이를 대표하는 전통 탈")
    private String content;

    @Schema(description = "희귀도", example = "BLUE")
    private Rarity rarity;

    @Schema(description = "앞면 이미지 URL", example = "https://example.com/seal-front.jpg")
    private String frontImageUrl;

    @Schema(description = "뒷면 이미지 URL", example = "https://example.com/seal-back.jpg")
    private String backImageUrl;
  }

  @Builder
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(title = "SealList : 띠부씰 목록 DTO")
  public static class SealListDTO {
    @Schema(description = "전체 띠부씰 수", example = "10")
    private int totalCount;

    @Schema(description = "띠부씰 목록")
    private List<SealDTO> seals;
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(title = "NearbySeal : 주변 띠부씰 DTO")
  public static class NearbySealDTO {
    @Schema(description = "띠부씰 ID", example = "2")
    private Long sealId;

    @Schema(description = "띠부씰 번호", example = "2")
    private Integer number;

    @Schema(description = "희귀도", example = "BLUE")
    private Rarity rarity;

    @Schema(description = "앞면 이미지 URL", example = "https://example.com/seal-front.jpg")
    private String frontImageUrl;

    @Schema(description = "관광지 이름", example = "불국사")
    @com.fasterxml.jackson.annotation.JsonProperty("spot_name")
    private String spotName;

    @Schema(description = "지역 이름", example = "경주시")
    @com.fasterxml.jackson.annotation.JsonProperty("location_name")
    private String locationName;

    @Schema(description = "위도", example = "35.7901")
    private Double latitude;

    @Schema(description = "경도", example = "129.3320")
    private Double longitude;

    @Schema(description = "현재 위치로부터의 거리 (m)", example = "500")
    private Integer distance;
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(title = "NearbySealList : 주변 띠부씰 목록 DTO")
  public static class NearbySealListDTO {
    @Schema(description = "주변 띠부씰 목록")
    private List<NearbySealDTO> nearbySeals;
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(title = "CollectSealResult : 띠부씰 획득 결과 DTO")
  public static class CollectSealResultDTO {
    @Schema(description = "띠부씰 ID", example = "1")
    private Long id;

    @Schema(description = "획득 성공 여부", example = "true")
    private boolean success;

    @Schema(description = "현재 위치와의 거리 (m)", example = "150")
    private Integer distance;
  }
}
