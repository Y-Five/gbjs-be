/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tradition.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
  public TraditionResponse createTradition(
      TraditionType type, TraditionRequest request, MultipartFile image) {

    PathName pathName =
        type == TraditionType.SPECIALTIES ? PathName.SPECIALTIES : PathName.ACTIVITY;

    Tradition tradition =
        Tradition.builder()
            .type(type)
            .address(request.getAddress())
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .imageUrl(s3Service.uploadFile(pathName, image))
            .build();

    traditionRepository.save(tradition);

    log.info("전통문화 정보 생성 - type: {}, name: {}", type, request.getName());

    return traditionMapper.toTraditionResponse(tradition);
  }

  @Override
  public PageResponse<TraditionResponse> getTraditions(TraditionType type, Pageable pageable) {
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
  public void deleteTradition(Long id) {
    Tradition tradition =
        traditionRepository
            .findById(id)
            .orElseThrow(
                () -> {
                  log.error("삭제 실패 - 전통문화 정보가 존재하지 않음. id: {}", id);
                  return new CustomException(TraditionErrorStatus.TRADITION_NOT_FOUND);
                });

    traditionRepository.delete(tradition);
    s3Service.deleteFile(s3Service.extractKeyNameFromUrl(tradition.getImageUrl()));

    log.info("전통문화 정보 삭제 - id: {}, name: {}", tradition.getId(), tradition.getName());
  }
}
