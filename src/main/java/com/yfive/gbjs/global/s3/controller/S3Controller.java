/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.s3.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.yfive.gbjs.global.common.response.ApiResponse;
import com.yfive.gbjs.global.s3.dto.S3Response;
import com.yfive.gbjs.global.s3.entity.PathName;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "S3", description = "이미지 관리 API")
@RequestMapping("/api/s3")
public interface S3Controller {

  @Operation(summary = "이미지 업로드 API", description = "이미지를 업로드하고 URL을 리턴받는 API")
  @PostMapping(value = "/image-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<ApiResponse<S3Response>> uploadImage(
      @RequestParam PathName pathName, MultipartFile file);

  @Operation(summary = "S3 파일 전체 조회 API", description = "해당 경로의 모든 파일 목록을 조회합니다.")
  @GetMapping("/image-list")
  ResponseEntity<ApiResponse<List<String>>> listFiles(@RequestParam PathName pathName);

  @Operation(summary = "S3 파일 삭제 API", description = "파일명을 기반으로 이미지를 삭제합니다.")
  @DeleteMapping("/{pathName}/{fileName}")
  ResponseEntity<ApiResponse<String>> deleteFile(
      @PathVariable PathName pathName, @PathVariable String fileName);
}
