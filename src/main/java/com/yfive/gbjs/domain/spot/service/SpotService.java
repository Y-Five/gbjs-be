package com.yfive.gbjs.domain.spot.service;

import com.yfive.gbjs.domain.spot.dto.response.SpotPageResponse;
import com.yfive.gbjs.domain.spot.dto.response.SpotResponse;
import org.springframework.data.domain.Pageable;

public interface SpotService {

  SpotPageResponse getSpotsByKeyword(String keyword, Pageable pageable,
      String sortBy, Double longitude, Double latitude);

  SpotResponse getSpotByContentId(Long contentId, Double longitude, Double latitude);
}
