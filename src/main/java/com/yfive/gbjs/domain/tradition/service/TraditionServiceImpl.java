/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tradition.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yfive.gbjs.domain.tradition.dto.request.TraditionRequest;
import com.yfive.gbjs.domain.tradition.dto.response.TraditionResponse;
import com.yfive.gbjs.domain.tradition.entity.Tradition;
import com.yfive.gbjs.domain.tradition.entity.TraditionType;
import com.yfive.gbjs.domain.tradition.exception.TraditionErrorStatus;
import com.yfive.gbjs.domain.tradition.mapper.TraditionMapper;
import com.yfive.gbjs.domain.tradition.repository.TraditionRepository;
import com.yfive.gbjs.global.error.exception.CustomException;
import com.yfive.gbjs.global.page.dto.response.PageResponse;
import com.yfive.gbjs.global.page.mapper.PageMapper;
import com.yfive.gbjs.global.s3.service.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TraditionServiceImpl implements TraditionService {

  private final TraditionRepository traditionRepository;
  private final TraditionMapper traditionMapper;
  private final PageMapper pageMapper;
  private final S3Service s3Service;

  @Override
  @Transactional
  public TraditionResponse createTradition(TraditionType type, TraditionRequest request) {

    Tradition tradition =
        Tradition.builder()
            .imageUrl(request.getImageUrl())
            .address(request.getAddress())
            .name(request.getName())
            .description(request.getDescription())
            .redirectUrl(request.getRedirectUrl())
            .type(type)
            .price(request.getPrice())
            .build();

    traditionRepository.save(tradition);

    log.info("전통문화 정보 생성 - type: {}, name: {}", type, request.getName());

    return traditionMapper.toTraditionResponse(tradition);
  }

  @Override
  public PageResponse<TraditionResponse> getTraditionsByType(
      TraditionType type, Pageable pageable) {

    Page<TraditionResponse> page =
        traditionRepository.findByType(type, pageable).map(traditionMapper::toTraditionResponse);

    log.info("전통문화 리스트 조회 - type: {}", type);

    return pageMapper.toTraditionPageResponse(page);
  }

  @Override
  @Transactional
  public TraditionResponse updateTradition(Long id, TraditionRequest request) {

    Tradition tradition =
        traditionRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(TraditionErrorStatus.TRADITION_NOT_FOUND));

    tradition.update(request);

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

    traditionRepository.delete(tradition);

    log.info("전통문화 정보 삭제 - id: {}, name: {}", tradition.getId(), tradition.getName());
  }
}
