/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class StoryBasedListResponse {

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
    private List<StoryItem> item;
  }

  @Data
  public static class StoryItem {

    @JsonProperty("tid")
    private String tid;

    @JsonProperty("tlid")
    private String tlid;

    @JsonProperty("stid")
    private String stid;

    @JsonProperty("stlid")
    private String stlid;

    @JsonProperty("title")
    private String title;

    @JsonProperty("addr1")
    private String addr1;

    @JsonProperty("addr2")
    private String addr2;

    @JsonProperty("audioTitle")
    private String audioTitle;

    @JsonProperty("script")
    private String script;

    @JsonProperty("playTime")
    private String playTime;

    @JsonProperty("audioUrl")
    private String audioUrl;

    @JsonProperty("imageUrl")
    private String imageUrl;

    @JsonProperty("langCode")
    private String langCode;

    @JsonProperty("createdtime")
    private String createdtime;

    @JsonProperty("modifiedtime")
    private String modifiedtime;
  }
}
