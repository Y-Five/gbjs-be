/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tradition.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.yfive.gbjs.domain.tradition.dto.request.TraditionRequest;
import com.yfive.gbjs.domain.tradition.dto.response.TraditionResponse;
import com.yfive.gbjs.domain.tradition.entity.Tradition;
import com.yfive.gbjs.domain.tradition.entity.TraditionType;
import com.yfive.gbjs.domain.tradition.exception.TraditionErrorStatus;
import com.yfive.gbjs.domain.tradition.mapper.TraditionMapper;
import com.yfive.gbjs.domain.tradition.repository.TraditionRepository;
import com.yfive.gbjs.global.common.response.PageResponse;
import com.yfive.gbjs.global.error.exception.CustomException;
import com.yfive.gbjs.global.s3.entity.PathName;
import com.yfive.gbjs.global.s3.exception.S3ErrorStatus;
import com.yfive.gbjs.global.s3.service.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TraditionServiceImpl implements TraditionService {

  private final TraditionRepository traditionRepository;
  private final TraditionMapper traditionMapper;
  private final S3Service s3Service;

  @Override
  @Transactional
  public TraditionResponse createTradition(
      TraditionType type, TraditionRequest request, MultipartFile image) {

    PathName pathName =
        type == TraditionType.SPECIALTIES ? PathName.SPECIALTIES : PathName.ACTIVITY;
    String imageUrl;

    try {
      imageUrl = s3Service.uploadFile(pathName, image);
    } catch (Exception e) {
      log.error("S3 파일 업로드 실패 - name: {}", request.getName(), e);
      throw new CustomException(S3ErrorStatus.FILE_SERVER_ERROR);
    }

    Tradition tradition =
        Tradition.builder()
            .imageUrl(imageUrl)
            .address(request.getAddress())
            .name(request.getName())
            .description(request.getDescription())
            .redirectUrl(request.getRedirectUrl())
            .type(type)
            .price(request.getPrice())
            .build();

    try {
      traditionRepository.save(tradition);
    } catch (Exception e) {
      s3Service.deleteFile(s3Service.extractKeyNameFromUrl(imageUrl));
      throw e;
    }

    log.info("전통문화 정보 생성 - type: {}, name: {}", type, request.getName());

    return traditionMapper.toTraditionResponse(tradition);
  }

  @Override
  public PageResponse<TraditionResponse> getTraditionsByType(
      TraditionType type, Pageable pageable) {

    Page<TraditionResponse> responsePage =
        traditionRepository.findByType(type, pageable).map(traditionMapper::toTraditionResponse);

    log.info("전통문화 리스트 조회 - type: {}", type);

    return PageResponse.<TraditionResponse>builder()
        .content(responsePage.getContent())
        .totalElements(responsePage.getTotalElements())
        .totalPages(responsePage.getTotalPages())
        .pageNum(responsePage.getNumber())
        .pageSize(responsePage.getSize())
        .last(responsePage.isLast())
        .first(responsePage.isFirst())
        .build();
  }

  @Override
  @Transactional
  public TraditionResponse updateTradition(Long id, TraditionRequest request, MultipartFile image) {

    Tradition tradition =
        traditionRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(TraditionErrorStatus.TRADITION_NOT_FOUND));

    PathName pathName =
        tradition.getType() == TraditionType.SPECIALTIES ? PathName.SPECIALTIES : PathName.ACTIVITY;

    String imageUrl = tradition.getImageUrl();

    if (image != null && !image.isEmpty()) {
      try {
        s3Service.deleteFile(s3Service.extractKeyNameFromUrl(imageUrl));
        imageUrl = s3Service.uploadFile(pathName, image);
      } catch (Exception e) {
        log.error("S3 파일 교체 실패 - id: {}", id, e);
        throw new CustomException(S3ErrorStatus.FILE_SERVER_ERROR);
      }
    }

    tradition.update(request, imageUrl);

    log.info("전통문화 정보 수정 - id: {}, name: {}", tradition.getId(), tradition.getName());

    return traditionMapper.toTraditionResponse(tradition);
  }

  @Override
  @Transactional
  public void deleteTradition(Long id) {

    Tradition tradition =
        traditionRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(TraditionErrorStatus.TRADITION_NOT_FOUND));

    String imageUrl = tradition.getImageUrl();

    try {
      s3Service.deleteFile(s3Service.extractKeyNameFromUrl(imageUrl));
    } catch (Exception e) {
      log.warn("S3 파일 삭제 실패 - imageUrl: {}", imageUrl, e);
    }

    traditionRepository.delete(tradition);

    log.info("전통문화 정보 삭제 - id: {}, name: {}", tradition.getId(), tradition.getName());
  }
}
