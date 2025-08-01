package com.yfive.gbjs.domain.spot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "SpotPageResponse DTO", description = "관광지 리스트 페이지 응답 반환")
public class SpotPageResponse {

  @Schema(description = "데이터 리스트")
  private List<SpotResponse> content;

  @Schema(description = "전체 데이터의 개수", example = "200")
  private Long totalElements;

  @Schema(description = "전체 페이지 개수", example = "50")
  private Integer totalPages;

  @Schema(description = "페이지 번호", example = "0")
  private Integer pageNum;

  @Schema(description = "페이지 크기", example = "4")
  private Integer pageSize;

  @Schema(description = "첫 번째 데이터 여부", example = "true")
  private Boolean first;

  @Schema(description = "마지막 데이터 여부", example = "false")
  private Boolean last;
}
