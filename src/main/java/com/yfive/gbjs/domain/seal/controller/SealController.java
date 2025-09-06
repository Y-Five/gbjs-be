/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.yfive.gbjs.domain.seal.dto.response.PopularSealSpotResponse;
import com.yfive.gbjs.domain.seal.dto.response.SealProductResponse;
import com.yfive.gbjs.domain.seal.dto.response.SealResponse;
import com.yfive.gbjs.domain.seal.dto.response.UserSealResponse;
import com.yfive.gbjs.domain.seal.entity.SortBy;
import com.yfive.gbjs.global.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/** 띠부씰 컨트롤러 인터페이스 띠부씰 관련 API 엔드포인트를 정의 */
@Tag(name = "띠부씰", description = "띠부씰 관련 API")
@RequestMapping("/api/seals")
public interface SealController {

  @GetMapping("/{sealId}")
  @Operation(summary = "띠부씰 단일 조회", description = "ID로 특정 띠부씰의 정보를 조회합니다.")
  ResponseEntity<ApiResponse<SealResponse.SealDTO>> getSealById(
      @PathVariable @Parameter(description = "조회할 띠부씰 ID", required = true, example = "1")
          Long sealId);

  @GetMapping("/spot/{sealSpotId}")
  @Operation(summary = "관광지로 띠부씰 조회", description = "SealSpotID로 띠부씰을 조회합니다.")
  ResponseEntity<ApiResponse<UserSealResponse.UserSealDTO>> searchSeals(
      @Parameter(hidden = true) Authentication authentication,
      @PathVariable @Parameter(description = "검색할 SealSpotId", required = true, example = "2")
          Long sealSpotId);

  @GetMapping("location")
  @Operation(summary = "행정구역 띠부씰 조회", description = "행정구역 띠부씰 목록을 조회합니다.")
  ResponseEntity<ApiResponse<SealResponse.SealListDTO>> getAllSeals(
      @RequestParam(required = false, defaultValue = "NUMBER")
          @Parameter(description = "정렬 옵션 (NUMBER: 번호순, RARITY: 희귀도순, LOCATION: 지역순)")
          SortBy sortBy,
      @RequestParam(required = false)
          @Parameter(description = "지역 이름 (여러 개 가능)", example = "안동시,경주시")
          List<String> locationNames);

  @GetMapping("/user")
  @Operation(summary = "회원 띠부씰 조회", description = "로그인된 회원의 띠부씰 수집 현황을 조회합니다. (수집한 것/수집하지 않은 것 포함)")
  ResponseEntity<ApiResponse<UserSealResponse.UserSealListDTO>> getMySeals(
      @Parameter(hidden = true) Authentication authentication,
      @RequestParam(required = false, defaultValue = "NUMBER")
          @Parameter(
              description = "정렬 옵션 (NUMBER: 번호순, RARITY: 희귀도순, LOCATION: 지역순, COLLECTED: 수집순)")
          SortBy sortBy);

  @GetMapping("/user/count")
  @Operation(summary = "회원 띠부씰 수집 개수 조회", description = "로그인된 회원이 수집한 띠부씰의 총 개수를 조회합니다.")
  ResponseEntity<ApiResponse<UserSealResponse.SealCountResponseDTO>> getMySealsCount(
      @Parameter(hidden = true) Authentication authentication);

  @PostMapping(value = "/{sealId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "[개발자]띠부씰 이미지&시 등록",
      description = "특정 띠부씰의 앞면, 뒷면 이미지와 시를 등록합니다. 모든 항목은 선택사항입니다.")
  ResponseEntity<ApiResponse<SealResponse.SealDTO>> uploadSealImages(
      @PathVariable @Parameter(description = "이미지를 등록할 띠부씰 ID") Long sealId,
      @RequestPart(value = "frontImage", required = false) @Parameter(description = "띠부씰 앞면 이미지 파일")
          MultipartFile frontImage,
      @RequestPart(value = "backImage", required = false) @Parameter(description = "띠부씰 뒷면 이미지 파일")
          MultipartFile backImage,
      @RequestParam(value = "content", required = false) @Parameter(description = "띠부씰 시")
          String content);

  @GetMapping("/products")
  @Operation(summary = "띠부씰 상품 조회", description = "구매 가능한 띠부씰 상품 목록을 조회합니다.")
  ResponseEntity<ApiResponse<SealProductResponse.SealProductListDTO>> getSealProducts();

  @GetMapping("/nearby")
  @Operation(summary = "주변 띠부씰 조회", description = "현재 위치에서 가장 가까운 띠부씰 4개를 조회합니다.")
  ResponseEntity<ApiResponse<SealResponse.NearbySealListDTO>> getNearbySeals(
      @RequestParam @Parameter(description = "현재 위치 위도", required = true, example = "35.79000")
          Double latitude,
      @RequestParam @Parameter(description = "현재 위치 경도", required = true, example = "129.33222")
          Double longitude);

  @PostMapping("/collect")
  @Operation(
      summary = "띠부씰 획득",
      description = "위치 인증을 통해 띠부씰을 획득합니다. 일반 지역은 500m, 울릉군은 2km 이내에 있어야 획득 가능합니다.")
  ResponseEntity<ApiResponse<SealResponse.CollectSealResultDTO>> collectSeal(
      @Parameter(hidden = true) Authentication authentication,
      @RequestParam @Parameter(description = "띠부씰 ID", required = true, example = "2") Long sealId,
      @RequestParam @Parameter(description = "현재 위치 위도", required = true, example = "35.79000")
          Double latitude,
      @RequestParam @Parameter(description = "현재 위치 경도", required = true, example = "129.33222")
          Double longitude);

  @DeleteMapping("/collect/{sealId}")
  @Operation(summary = "획득한 띠부씰 삭제", description = "사용자가 획득한 띠부씰을 삭제합니다.")
  ResponseEntity<ApiResponse<Void>> deleteCollectedSeal(
      @Parameter(hidden = true) Authentication authentication,
      @PathVariable @Parameter(description = "삭제할 띠부씰 ID", required = true, example = "2")
          Long sealId);

  @GetMapping("/popular-spots")
  @Operation(summary = "인기 띠부씰 관광지 조회", description = "인기 띠부씰 관광지 4개를 조회합니다.")
  ResponseEntity<ApiResponse<List<PopularSealSpotResponse>>> getPopularSealSpots();
}
