/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tradition.controller;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.yfive.gbjs.domain.tradition.dto.request.TraditionRequest;
import com.yfive.gbjs.domain.tradition.dto.response.TraditionResponse;
import com.yfive.gbjs.domain.tradition.entity.TraditionType;
import com.yfive.gbjs.global.common.response.ApiResponse;
import com.yfive.gbjs.global.common.response.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "전통문화", description = "전통문화 관련 API")
@RequestMapping("/api/traditions")
public interface TraditionController {

  @PostMapping(value = "/dev", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "[개발자]전통문화 생성", description = "스웨거를 사용해 경상북도 전통문화(특산품/체험) 생성")
  ResponseEntity<ApiResponse<TraditionResponse>> createTradition(
      @Parameter(description = "전통문화 분류", example = "SPECIALTIES") @RequestParam TraditionType type,
      @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
          @RequestPart(value = "tradition")
          @Valid
          TraditionRequest request,
      @Parameter(
              description = "전통문화 이미지",
              content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
          @RequestPart(value = "image")
          MultipartFile image);

  @GetMapping
  @Operation(summary = "전통문화 리스트 조회", description = "경상북도 전통문화 리스트 반환")
  ResponseEntity<ApiResponse<PageResponse<TraditionResponse>>> getTraditions(
      @Parameter(description = "전통문화 분류", example = "SPECIALTIES") @RequestParam TraditionType type,
      @Parameter(description = "페이지 번호", example = "0") @RequestParam Integer pageNum,
      @Parameter(description = "페이지 크기", example = "4") @RequestParam Integer pageSize);

  @PutMapping("/{id}")
  @Operation(summary = "[개발자]전통문화 수정", description = "스웨거를 사용해 경상북도 전통문화(특산품/체험) 수정")
  ResponseEntity<ApiResponse<TraditionResponse>> updateTradition(
      @PathVariable Long id, @RequestBody @Valid TraditionRequest request, String imageUrl);

  @DeleteMapping("/dev/{id}")
  @Operation(summary = "[개발자]전통문화 삭제", description = "스웨거를 사용해 경상북도 전통문화(특산품/체험) 삭제")
  ResponseEntity<ApiResponse<String>> deleteTradition(@PathVariable Long id);
}
