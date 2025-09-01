/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordinateValidationResponse {
  private int total;
  private int insideGyeongbuk;
  private int outsideGyeongbuk;
  private double insidePercentage;
  private List<OutsideData> outsideDataList;

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OutsideData {
    private String title;
    private double latitude;
    private double longitude;
  }
}
