package com.yfive.gbjs.global.common.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class PageResponse<T> {

  private List<T> content;
  private Pagination pagination;

  @Getter
  @AllArgsConstructor
  public static class Pagination {

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean isFirst;
    private boolean isLast;
    private boolean hasNext;
    private boolean hasPrevious;
  }
}