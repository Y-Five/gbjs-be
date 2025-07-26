/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.yfive.gbjs.domain.seal.entity.Location;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserSealResponse {

  @Builder
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(title = "UserSeal : 사용자 띠부씰 수집 현황 DTO")
  public static class UserSealDTO {
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

    @Schema(description = "이미지 URL", example = "https://example.com/seal.jpg")
    private String imageUrl;

    @Schema(description = "수집 여부", example = "true")
    private boolean collected;

    @Schema(description = "수집 일시", example = "2025-01-26T10:30:00")
    private LocalDateTime collectedAt;
  }

  @Builder
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(title = "UserSealList : 사용자 띠부씰 수집 현황 목록 DTO")
  public static class UserSealListDTO {
    @Schema(description = "사용자 띠부씰 수집 현황 목록")
    private List<UserSealDTO> seals;

    @Schema(description = "전체 띠부씰 수", example = "10")
    private int totalCount;

    @Schema(description = "수집한 띠부씰 수", example = "3")
    private int collectedCount;
  }
}
