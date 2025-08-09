/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tradition.entity;

import com.yfive.gbjs.domain.tradition.dto.request.TraditionRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "tradition")
public class Tradition {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "imageUrl", nullable = false)
  private String imageUrl;

  @Column(name = "address", nullable = false)
  private String address;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "redirectUrl", nullable = false)
  private String redirectUrl;

  @Column(name = "type", nullable = false)
  private TraditionType type;

  @Column(name = "price", nullable = false)
  private Long price;


  public void update(TraditionRequest request, String imageUrl) {
    this.imageUrl = imageUrl;
    this.address = request.getAddress();
    this.name = request.getName();
    this.description = request.getDescription();
    this.redirectUrl = request.getRedirectUrl();
    this.price = request.getPrice();
  }
}
