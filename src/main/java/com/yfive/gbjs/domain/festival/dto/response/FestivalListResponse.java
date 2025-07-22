package com.yfive.gbjs.domain.festival.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "FestivalListResponse DTO", description = "축제 리스트 응답 반환")
public class FestivalListResponse {

  @Schema(description = "전체 데이터의 개수", example = "100")
  private Integer totalCount;

  @Schema(description = "조회한 데이터의 다음 인덱스 값", example = "3")
  private Integer nextIndex;

  @Schema(description = "축제 리스트")
  private List<FestivalResponse> festivalList;
}
