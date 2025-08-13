/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.dto.request;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "CollectSealRequest : 띠부씰 획득 요청 DTO")
public class CollectSealRequest {

  @NotNull(message = "띠부씰 ID는 필수입니다")
  @Schema(description = "획득할 띠부씰 ID", example = "1", required = true)
  private Long sealId;

  @NotNull(message = "현재 위도는 필수입니다")
  @Schema(description = "현재 위치 위도", example = "35.789769", required = true)
  private Double latitude;

  @NotNull(message = "현재 경도는 필수입니다")
  @Schema(description = "현재 위치 경도", example = "129.332094", required = true)
  private Double longitude;
}
