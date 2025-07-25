/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.course.dto.response;

import com.yfive.gbjs.domain.course.entity.CourseSpot;
import com.yfive.gbjs.domain.seal.entity.Category;
import com.yfive.gbjs.domain.seal.entity.Location;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 코스 장소 응답 DTO
 * 코스에 포함된 각 방문 장소(띠부씰) 정보를 전달
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSpotResponse {

  private Long sealId;
  private String sealName;
  private Location location;
  private String content;
  private Category category;
  private List<String> hashtagList;
  private Integer order;

  /**
   * CourseSpot 엔티티를 CourseSpotResponse DTO로 변환
   *
   * @param courseSpot 코스 장소 엔티티
   * @return 코스 장소 응답 DTO
   */
  public static CourseSpotResponse of(CourseSpot courseSpot) {
    return CourseSpotResponse.builder()
        .sealId(courseSpot.getSeal().getId())
        .sealName(courseSpot.getSeal().getName())
        .location(courseSpot.getSeal().getLocation())
        .content(courseSpot.getSeal().getContent())
        .category(courseSpot.getSeal().getCategory())
        .hashtagList(courseSpot.getSeal().getHashtagList())
        .order(courseSpot.getOrder())
        .build();
  }
}