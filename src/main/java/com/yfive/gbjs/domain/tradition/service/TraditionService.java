/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tradition.service;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.yfive.gbjs.domain.tradition.dto.request.TraditionRequest;
import com.yfive.gbjs.domain.tradition.dto.response.TraditionResponse;
import com.yfive.gbjs.domain.tradition.entity.TraditionType;
import com.yfive.gbjs.global.common.response.PageResponse;

public interface TraditionService {

  PageResponse<TraditionResponse> getTraditions(TraditionType type, Pageable pageable);

  TraditionResponse createTradition(
      TraditionType type, TraditionRequest request, MultipartFile image);

  TraditionResponse updateTradition(Long id, TraditionRequest request, String imageUrl);

  void deleteTradition(Long id);
}
