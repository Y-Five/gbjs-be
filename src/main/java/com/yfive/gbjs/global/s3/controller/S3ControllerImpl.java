/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.s3.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yfive.gbjs.global.common.response.ApiResponse;
import com.yfive.gbjs.global.s3.dto.S3Response;
import com.yfive.gbjs.global.s3.entity.PathName;
import com.yfive.gbjs.global.s3.service.S3Service;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class S3ControllerImpl implements S3Controller {

  private final S3Service s3Service;

  @Override
  public ResponseEntity<ApiResponse<S3Response>> uploadImage(
      @RequestParam PathName pathName, MultipartFile file) {

    S3Response s3Response = s3Service.uploadImage(pathName, file);
    return ResponseEntity.ok(ApiResponse.success(s3Response));
  }

  @Override
  public ResponseEntity<ApiResponse<List<String>>> listFiles(@RequestParam PathName pathName) {
    List<String> files = s3Service.getAllFiles(pathName);
    return ResponseEntity.ok(ApiResponse.success(files));
  }

  @Override
  public ResponseEntity<ApiResponse<String>> deleteFile(
      @PathVariable PathName pathName, @PathVariable String fileName) {
    s3Service.deleteFile(pathName, fileName);
    return ResponseEntity.ok(ApiResponse.success());
  }
}
