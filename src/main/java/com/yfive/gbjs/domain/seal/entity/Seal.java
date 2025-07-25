package com.yfive.gbjs.domain.seal.entity;

import com.yfive.gbjs.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 띠부씰 엔티티
 * 강원도의 각 지역별 관광 명소 정보를 담는 엔티티
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "seal")
public class Seal extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * 띠부씰 이름 (관광지명)
   */
  @Column(name = "name", nullable = false, unique = true)
  private String name;

  /**
   * 띠부씰 위치 (지역)
   */
  @Column(name = "location", nullable = false)
  private Location location;

  /**
   * 띠부씰 설명
   */
  @Column(name = "content", nullable = false)
  private String content;

  /**
   * 띠부씰 카테고리 (관광지 유형)
   */
  @Column(name = "category", nullable = false)
  private Category category;

  /**
   * 해시태그 목록
   */
  @Column(name = "hashtag_list", nullable = false)
  private List<String> hashtagList;
}
