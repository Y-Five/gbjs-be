/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.yfive.gbjs.domain.seal.entity.SealSpotCategory;

@Converter(autoApply = true)
public class SealSpotCategoryConverter implements AttributeConverter<SealSpotCategory, String> {

  @Override
  public String convertToDatabaseColumn(SealSpotCategory category) {
    if (category == null) {
      return null;
    }
    return switch (category) {
      case NATURE -> "자연환경";
      case NIGHTSCAPE -> "야경 명소";
      case HEALING -> "힐링 명소";
      case ATTRACTION -> "유명 관광지";
      case ACTIVITY -> "액티비티";
    };
  }

  @Override
  public SealSpotCategory convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    }
    return switch (dbData) {
      case "자연환경" -> SealSpotCategory.NATURE;
      case "야경 명소" -> SealSpotCategory.NIGHTSCAPE;
      case "힐링 명소" -> SealSpotCategory.HEALING;
      case "유명 관광지" -> SealSpotCategory.ATTRACTION;
      case "액티비티" -> SealSpotCategory.ACTIVITY;
      default -> throw new IllegalArgumentException("Unknown category value: " + dbData);
    };
  }
}
