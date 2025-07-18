/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ThemeBasedListResponse {

  @JsonProperty("items")
  private Items items;

  @JsonProperty("numOfRows")
  private Integer numOfRows;

  @JsonProperty("pageNo")
  private Integer pageNo;

  @JsonProperty("totalCount")
  private Integer totalCount;

  @Data
  public static class Items {

    @JsonProperty("item")
    private List<ThemeItem> item;
  }

  @Data
  public static class ThemeItem {

    @JsonProperty("tid")
    private String tid;

    @JsonProperty("tlid")
    private String tlid;

    @JsonProperty("title")
    private String title;

    @JsonProperty("themaCategory")
    private String themaCategory;

    @JsonProperty("addr1")
    private String addr1;

    @JsonProperty("addr2")
    private String addr2;

    @JsonProperty("mapX")
    private Double mapX;

    @JsonProperty("mapY")
    private Double mapY;

    @JsonProperty("langCode")
    private String langCode;

    @JsonProperty("langCheck")
    private String langCheck;

    @JsonProperty("imageUrl")
    private String imageUrl;

    @JsonProperty("createdtime")
    private String createdtime;

    @JsonProperty("modifiedtime")
    private String modifiedtime;
  }
}
