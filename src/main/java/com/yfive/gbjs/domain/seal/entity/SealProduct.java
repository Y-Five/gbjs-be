package com.yfive.gbjs.domain.seal.entity;

import com.yfive.gbjs.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 띠부씰 상품 엔티티
 * 띠부씰과 관련된 기념품/상품 정보를 관리
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "seal_product")
public class SealProduct extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 상품명
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * 상품 설명
     */
    @Column(name = "description", nullable = false)
    private String description;

    /**
     * 가격
     */
    @Column(name = "price", nullable = false)
    private Long price;

    /**
     * 상품 이미지 URL
     */
    @Column(name = "image_url")
    private String imageUrl;
}
