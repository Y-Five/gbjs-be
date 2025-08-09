/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tradition.service;

import com.yfive.gbjs.domain.tradition.dto.request.TraditionRequest;
import com.yfive.gbjs.domain.tradition.dto.response.TraditionResponse;
import com.yfive.gbjs.domain.tradition.entity.TraditionType;
import com.yfive.gbjs.global.common.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface TraditionService {

  TraditionResponse createTradition(TraditionType type, TraditionRequest request,
      MultipartFile image);

  PageResponse<TraditionResponse> getTraditionsByType(TraditionType type, Pageable pageable);

  TraditionResponse updateTradition(Long id, TraditionRequest request, MultipartFile image);

  void deleteTradition(Long id);
}
