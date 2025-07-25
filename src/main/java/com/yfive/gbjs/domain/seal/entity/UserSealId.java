/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserSealId implements Serializable {

  @Column(name = "user_id")
  private Long userId;

  @Column(name = "seal_id")
  private Long sealId;
}