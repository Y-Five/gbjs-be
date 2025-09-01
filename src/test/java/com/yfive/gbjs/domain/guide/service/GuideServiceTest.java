/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.guide.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfive.gbjs.domain.guide.converter.AudioGuideConverter;
import com.yfive.gbjs.domain.guide.entity.AudioGuide;
import com.yfive.gbjs.domain.guide.repository.AudioGuideRepository;
import com.yfive.gbjs.domain.guide.util.GeoJsonBoundaryChecker;

@ExtendWith(MockitoExtension.class)
class GuideServiceTest {

  @Mock private RestClient restClient;

  @Mock private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

  @Mock private RestClient.ResponseSpec responseSpec;

  @Mock private ObjectMapper objectMapper;

  @Mock private AudioGuideRepository audioGuideRepository;

  @Mock private AudioGuideConverter audioGuideConverter;

  @Mock private GeoJsonBoundaryChecker geoJsonBoundaryChecker;

  @InjectMocks private GuideServiceImpl guideService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(guideService, "audioApiHost", "http://test.api.com");
    ReflectionTestUtils.setField(guideService, "serviceKey", "testKey");
  }

  @Test
  @DisplayName("updateFromSync 메서드가 tid를 포함한 모든 필드를 업데이트하는지 테스트")
  void testUpdateFromSyncIncludesTid() {
    // Given
    AudioGuide existingGuide =
        AudioGuide.builder()
            .id(1L)
            .tid("OLD_TID")
            .title("Old Title")
            .audioTitle("Old Audio Title")
            .script("Old Script")
            .playTime(100)
            .audioUrl("old_audio.mp3")
            .build();

    AudioGuide updatedData =
        AudioGuide.builder()
            .tid("NEW_TID")
            .title("New Title")
            .audioTitle("New Audio Title")
            .script("New Script")
            .playTime(200)
            .audioUrl("new_audio.mp3")
            .audioGuideId("audio123")
            .stlid("stl123")
            .tlid("tl123")
            .longitude("128.5")
            .latitude("36.5")
            .imageUrl("image.jpg")
            .langCode("ko")
            .syncStatus("U")
            .apiModifiedTime("20250101120000")
            .apiCreatedTime("20250101100000")
            .lastSyncedAt("20250107150000")
            .build();

    // When
    existingGuide.updateFromSync(updatedData);

    // Then
    assertEquals("NEW_TID", existingGuide.getTid(), "tid가 업데이트되어야 함");
    assertEquals("New Title", existingGuide.getTitle());
    assertEquals("New Audio Title", existingGuide.getAudioTitle());
    assertEquals("New Script", existingGuide.getScript());
    assertEquals(200, existingGuide.getPlayTime());
    assertEquals("new_audio.mp3", existingGuide.getAudioUrl());
    assertEquals("audio123", existingGuide.getAudioGuideId());
    assertEquals("stl123", existingGuide.getStlid());
    assertEquals("tl123", existingGuide.getTlid());
    assertEquals("128.5", existingGuide.getLongitude());
    assertEquals("36.5", existingGuide.getLatitude());
    assertEquals("image.jpg", existingGuide.getImageUrl());
    assertEquals("ko", existingGuide.getLangCode());
    assertEquals("U", existingGuide.getSyncStatus());
    assertEquals("20250101120000", existingGuide.getApiModifiedTime());
    assertEquals("20250101100000", existingGuide.getApiCreatedTime());
    assertEquals("20250107150000", existingGuide.getLastSyncedAt());
  }

  @Test
  @DisplayName("동기화 시 기존 데이터 업데이트 테스트")
  void testSyncUpdatesExistingData() {
    // Given
    String tid = "TID123";
    AudioGuide existingGuide = AudioGuide.builder().id(1L).tid(tid).title("Old Title").build();

    when(audioGuideRepository.count()).thenReturn(1L);
    when(audioGuideRepository.findLatestModifiedTime()).thenReturn(Optional.empty());
    when(audioGuideRepository.findByTid(tid)).thenReturn(Optional.of(existingGuide));
    when(audioGuideRepository.findAll()).thenReturn(java.util.Collections.emptyList());
    when(geoJsonBoundaryChecker.isInGyeongbukRegion(anyDouble(), anyDouble())).thenReturn(true);

    String mockApiResponse =
        """
            {
                "response": {
                    "body": {
                        "totalCount": 1,
                        "numOfRows": 1000,
                        "items": {
                            "item": [{
                                "tid": "TID123",
                                "tlid": "tlid123",
                                "title": "New Title",
                                "mapX": "128.5",
                                "mapY": "36.5",
                                "langCode": "ko",
                                "imageUrl": "image.jpg",
                                "syncStatus": "U",
                                "createdtime": "20250101100000",
                                "modifiedtime": "20250101120000",
                                "stid": "audio123",
                                "stlid": "stl123",
                                "audioTitle": "Audio Title",
                                "script": "Script content",
                                "playTime": "180",
                                "audioUrl": "audio.mp3"
                            }]
                        }
                    }
                }
            }
            """;

    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(String.class)).thenReturn(mockApiResponse);

    try {
      when(objectMapper.readTree(mockApiResponse))
          .thenReturn(new ObjectMapper().readTree(mockApiResponse));
    } catch (Exception e) {
      fail("Mock setup failed");
    }

    // When
    int result = guideService.syncGyeongbukAudioStories();

    // Then
    verify(audioGuideRepository, times(1)).save(any(AudioGuide.class));
    assertEquals(1, result);
  }

  @Test
  @DisplayName("동기화 시 신규 데이터 저장 테스트")
  void testSyncSavesNewData() {
    // Given
    String tid = "NEW_TID";

    when(audioGuideRepository.count()).thenReturn(1L);
    when(audioGuideRepository.findLatestModifiedTime()).thenReturn(Optional.empty());
    when(audioGuideRepository.findByTid(tid)).thenReturn(Optional.empty());
    when(audioGuideRepository.findAll()).thenReturn(java.util.Collections.emptyList());
    when(geoJsonBoundaryChecker.isInGyeongbukRegion(anyDouble(), anyDouble())).thenReturn(true);

    String mockApiResponse =
        """
            {
                "response": {
                    "body": {
                        "totalCount": 1,
                        "numOfRows": 1000,
                        "items": {
                            "item": [{
                                "tid": "NEW_TID",
                                "tlid": "tlid123",
                                "title": "New Guide",
                                "mapX": "128.5",
                                "mapY": "36.5",
                                "langCode": "ko",
                                "imageUrl": "image.jpg",
                                "syncStatus": "A",
                                "createdtime": "20250101100000",
                                "modifiedtime": "20250101120000",
                                "stid": "audio123",
                                "stlid": "stl123",
                                "audioTitle": "Audio Title",
                                "script": "Script content",
                                "playTime": "180",
                                "audioUrl": "audio.mp3"
                            }]
                        }
                    }
                }
            }
            """;

    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(String.class)).thenReturn(mockApiResponse);

    try {
      when(objectMapper.readTree(mockApiResponse))
          .thenReturn(new ObjectMapper().readTree(mockApiResponse));
    } catch (Exception e) {
      fail("Mock setup failed");
    }

    // When
    int result = guideService.syncGyeongbukAudioStories();

    // Then
    verify(audioGuideRepository, times(1)).save(any(AudioGuide.class));
    verify(audioGuideRepository, never()).deleteByTid(anyString());
    assertEquals(1, result);
  }

  @Test
  @DisplayName("동기화 시 삭제 처리 테스트")
  void testSyncDeletesData() {
    // Given
    String tid = "DELETE_TID";

    when(audioGuideRepository.count()).thenReturn(1L);
    when(audioGuideRepository.findLatestModifiedTime()).thenReturn(Optional.empty());
    when(audioGuideRepository.findAll()).thenReturn(java.util.Collections.emptyList());
    when(geoJsonBoundaryChecker.isInGyeongbukRegion(anyDouble(), anyDouble())).thenReturn(true);

    String mockApiResponse =
        """
            {
                "response": {
                    "body": {
                        "totalCount": 1,
                        "numOfRows": 1000,
                        "items": {
                            "item": [{
                                "tid": "DELETE_TID",
                                "tlid": "tlid123",
                                "title": "To Delete",
                                "mapX": "128.5",
                                "mapY": "36.5",
                                "langCode": "ko",
                                "imageUrl": "image.jpg",
                                "syncStatus": "D",
                                "createdtime": "20250101100000",
                                "modifiedtime": "20250101120000"
                            }]
                        }
                    }
                }
            }
            """;

    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(String.class)).thenReturn(mockApiResponse);

    try {
      when(objectMapper.readTree(mockApiResponse))
          .thenReturn(new ObjectMapper().readTree(mockApiResponse));
    } catch (Exception e) {
      fail("Mock setup failed");
    }

    // When
    int result = guideService.syncGyeongbukAudioStories();

    // Then
    verify(audioGuideRepository, times(1)).deleteByTid(tid);
    verify(audioGuideRepository, never()).save(any(AudioGuide.class));
    assertEquals(1, result); // 삭제도 카운트에 포함됨
  }
}
