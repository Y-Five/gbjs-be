package com.yfive.gbjs.domain.weather.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "WeatherResponse DTO", description = "날씨 응답 반환")
public class WeatherResponse {

  @Schema(description = "최저 기온", example = "21")
  private String lowestTemperature; // TMN

  @Schema(description = "최고 기온", example = "32")
  private String highestTemperature; // TMX

  @Schema(description = "현재 기온", example = "29")
  private String temperature; // TMP

  @Schema(description = "날씨", example = "맑음")
  private String weather;

  @Schema(description = "강수 확률", example = "50")
  private String precipitation; // POP
}