package com.yfive.gbjs.domain.spot.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Schema(title = "SpotListResponse DTO", description = "관광지 응답 반환")
public class SpotListResponse {

  @JsonProperty("firstimage")
  @Schema(description = "관광지 사진 URL", example = "http://tong.visitkorea.or.kr/cms/resource/01/2656601_image2_1.jpg")
  private String imageUrl;

  @Schema(description = "관광지명", example = "첨성대")
  private String title;

  @JsonProperty("addr1")
  @Schema(description = "관광지 주소", example = "경상북도 경주시 첨성로 140-25")
  private String address;

  @JsonProperty("contentid")
  @Schema(description = "관광지 식별자", example = "126207")
  private String spotId;

  @Setter
  @Schema(description = "관광지 분류코드", example = "유적지/사적지")
  private String type;

  @Setter
  @Schema(description = "관광지까지의 거리", example = "129.218564")
  private Double distance;

  @Setter
  @Schema(description = "음성 가이드 여부", example = "true")
  private Boolean audio;
}

