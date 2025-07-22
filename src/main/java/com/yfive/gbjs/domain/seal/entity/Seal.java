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

  @Column(name = "name", nullable = false, unique = true)
  private String name;

  @Column(name = "location", nullable = false)
  private Location location;

  @Column(name = "content", nullable = false)
  private String content;

  @Column(name = "category", nullable = false)
  private Category category;

  @Column(name = "hashtag_list", nullable = false)
  private List<String> hashtagList;
}
