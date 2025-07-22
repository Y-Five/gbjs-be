package com.yfive.gbjs.domain.festival.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "FestivalResponse DTO", description = "축제 응답 반환")
public class FestivalResponse {

  @JsonProperty("firstimage2")
  @Schema(description = "포스터 URL", example = "http://tong.visitkorea.or.kr/cms/resource/69/3459169_image2_1.JPG")
  private String posterUrl;

  @JsonProperty("addr1")
  @Schema(description = "축제 지역", example = "경상북도 안동시")
  private String region;

  @Schema(description = "축제명", example = "안동 국제 탈춤 페스티벌")
  private String title;

  @JsonProperty("eventstartdate")
  @JsonFormat(pattern = "yyyyMMdd")
  @Schema(description = "시작일", example = "20250926")
  private LocalDate startDate;

  @JsonProperty("eventenddate")
  @JsonFormat(pattern = "yyyyMMdd")
  @Schema(description = "종료일", example = "20251005")
  private LocalDate endDate;
}
