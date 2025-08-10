/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.s3.service;

import java.io.InputStream;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.yfive.gbjs.global.s3.dto.S3Response;
import com.yfive.gbjs.global.s3.entity.PathName;

public interface S3Service {

  S3Response uploadImage(PathName pathName, MultipartFile file);

  String uploadFile(PathName pathName, MultipartFile file);

  String uploadFile(
      PathName pathName,
      Long guideId,
      InputStream inputStream,
      String originalFilename,
      String contentType);

  String createKeyName(PathName pathName);

  String createKeyName(PathName pathName, Long id);

  void deleteFile(String keyName);

  List<String> getAllFiles(PathName pathName);

  void deleteFile(PathName pathName, String fileName);

  String extractKeyNameFromUrl(String imageUrl);

  void fileExists(String keyName);
}
