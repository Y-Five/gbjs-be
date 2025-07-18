/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OpenApiBaseResponse<T> {

  @JsonProperty("response")
  private ResponseBody<T> response;

  @Data
  public static class ResponseBody<T> {

    @JsonProperty("header")
    private Header header;

    @JsonProperty("body")
    private T body;

    @Data
    public static class Header {

      @JsonProperty("resultCode")
      private String resultCode;

      @JsonProperty("resultMsg")
      private String resultMsg;
    }
  }
}
