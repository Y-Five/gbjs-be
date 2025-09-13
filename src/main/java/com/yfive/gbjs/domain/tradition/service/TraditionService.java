/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.tradition.service;

import org.springframework.data.domain.Pageable;

import com.yfive.gbjs.domain.tradition.dto.request.TraditionRequest;
import com.yfive.gbjs.domain.tradition.dto.response.TraditionResponse;
import com.yfive.gbjs.domain.tradition.entity.TraditionType;
import com.yfive.gbjs.global.page.dto.response.PageResponse;

public interface TraditionService {

  TraditionResponse createTradition(TraditionType type, TraditionRequest request);

  PageResponse<TraditionResponse> getTraditionsByType(TraditionType type, Pageable pageable);

  TraditionResponse updateTradition(Long id, TraditionRequest request);

  void deleteTradition(Long id);
}
