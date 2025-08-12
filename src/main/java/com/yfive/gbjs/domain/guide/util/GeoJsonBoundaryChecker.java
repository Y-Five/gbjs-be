/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeoJsonBoundaryChecker {

  private final ObjectMapper objectMapper;
  private List<List<Point>> polygons = new ArrayList<>();

  static class Point {
    double longitude;
    double latitude;

    public Point(double longitude, double latitude) {
      this.longitude = longitude;
      this.latitude = latitude;
    }
  }

  @PostConstruct
  public void loadGeoJsonBoundary() {
    try {
      ClassPathResource resource = new ClassPathResource("geojson/gyeongbuk.json");
      InputStream inputStream = resource.getInputStream();

      JsonNode root = objectMapper.readTree(inputStream);

      // FeatureCollection 또는 단일 Feature 처리
      if (root.has("features")) {
        // FeatureCollection
        JsonNode features = root.get("features");
        // log.info("FeatureCollection 발견: {}개 features", features.size());
        for (JsonNode feature : features) {
          parseGeometry(feature.get("geometry"));
        }
      } else if (root.has("geometry")) {
        // 단일 Feature
        // log.info("단일 Feature 발견");
        parseGeometry(root.get("geometry"));
      }

      log.info("경상북도 GeoJSON 경계 데이터 로드 완료: {}개 폴리곤", polygons.size());
    } catch (IOException e) {
      log.error("GeoJSON 파일 로드 실패: ", e);
      polygons.clear(); // 실패 시 빈 리스트로 초기화
    }
  }

  private void parseGeometry(JsonNode geometry) {
    String type = geometry.get("type").asText();
    JsonNode coordinates = geometry.get("coordinates");

    // log.info("Geometry 타입: {}", type);

    switch (type) {
      case "Polygon":
        // log.info("Polygon 처리 중...");
        parsePolygon(coordinates);
        break;
      case "MultiPolygon":
        // log.info("MultiPolygon 처리 중: {}개 폴리곤", coordinates.size());
        for (JsonNode polygonCoords : coordinates) {
          parsePolygon(polygonCoords);
        }
        break;
      default:
        log.warn("지원하지 않는 geometry 타입: {}", type);
    }
  }

  private void parsePolygon(JsonNode coordinates) {
    // 첫 번째 ring만 사용 (외곽선)
    JsonNode outerRing = coordinates.get(0);
    List<Point> polygon = new ArrayList<>();

    for (JsonNode coord : outerRing) {
      double longitude = coord.get(0).asDouble();
      double latitude = coord.get(1).asDouble();
      polygon.add(new Point(longitude, latitude));
    }

    if (polygon.size() > 2) {
      polygons.add(polygon);
    }
  }

  /**
   * Ray Casting Algorithm을 사용한 Point in Polygon 검사
   *
   * @param longitude 경도
   * @param latitude 위도
   * @param polygon 폴리곤 점들의 리스트
   * @return 폴리곤 내부에 있으면 true
   */
  private boolean isPointInPolygon(double longitude, double latitude, List<Point> polygon) {
    int intersections = 0;
    int n = polygon.size();

    for (int i = 0; i < n; i++) {
      Point p1 = polygon.get(i);
      Point p2 = polygon.get((i + 1) % n);

      // 수평 ray와 edge의 교차점 검사
      if (((p1.latitude > latitude) != (p2.latitude > latitude))
          && (longitude
              < (p2.longitude - p1.longitude)
                      * (latitude - p1.latitude)
                      / (p2.latitude - p1.latitude)
                  + p1.longitude)) {
        intersections++;
      }
    }

    return intersections % 2 == 1;
  }

  /**
   * 주어진 좌표가 경상북도 지역에 속하는지 확인
   *
   * @param latitude 위도
   * @param longitude 경도
   * @return 경북 지역 포함 여부
   */
  public boolean isInGyeongbukRegion(double latitude, double longitude) {
    if (polygons.isEmpty()) {
      log.error("GeoJSON 경계 데이터가 로드되지 않았습니다");
      return false;
    }

    // 1. 정확한 폴리곤 내부 검사
    for (List<Point> polygon : polygons) {
      if (isPointInPolygon(longitude, latitude, polygon)) {
        return true;
      }
    }

    // 2. 해안가 버퍼 검사 (폴리곤 외부이지만 가까운 거리)
    if (isNearCoastline(longitude, latitude)) {
      return true;
    }

    return false;
  }

  /** 해안선 근처인지 확인 (약 2km 버퍼) */
  private boolean isNearCoastline(double longitude, double latitude) {
    // 동해안 지역 확인 (경도 129.3 이상이면서 경북 위도 범위)
    if (longitude >= 129.3 && longitude <= 129.6 && latitude >= 35.6 && latitude <= 36.8) {
      // 해안가로부터 약 2km 이내 (경도 0.02도 ≈ 2km)
      for (List<Point> polygon : polygons) {
        double minDistance = getMinDistanceToPolygon(longitude, latitude, polygon);
        if (minDistance < 0.02) { // 약 2km
          log.debug("해안가 버퍼 적용: 거리 {}", minDistance);
          return true;
        }
      }
    }
    return false;
  }

  /** 점에서 폴리곤까지의 최소 거리 계산 (간단한 근사치) */
  private double getMinDistanceToPolygon(double longitude, double latitude, List<Point> polygon) {
    double minDistance = Double.MAX_VALUE;
    for (Point p : polygon) {
      double distance =
          Math.sqrt(Math.pow(longitude - p.longitude, 2) + Math.pow(latitude - p.latitude, 2));
      minDistance = Math.min(minDistance, distance);
    }
    return minDistance;
  }
}
