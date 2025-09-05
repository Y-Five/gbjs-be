/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.seal.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.yfive.gbjs.domain.seal.converter.SealConverter;
import com.yfive.gbjs.domain.seal.converter.SealProductConverter;
import com.yfive.gbjs.domain.seal.converter.UserSealConverter;
import com.yfive.gbjs.domain.seal.dto.response.SealProductResponse;
import com.yfive.gbjs.domain.seal.dto.response.SealResponse;
import com.yfive.gbjs.domain.seal.dto.response.UserSealResponse;
import com.yfive.gbjs.domain.seal.entity.Location;
import com.yfive.gbjs.domain.seal.entity.Rarity;
import com.yfive.gbjs.domain.seal.entity.Seal;
import com.yfive.gbjs.domain.seal.entity.SortBy;
import com.yfive.gbjs.domain.seal.entity.mapper.UserSeal;
import com.yfive.gbjs.domain.seal.exception.SealErrorStatus;
import com.yfive.gbjs.domain.seal.repository.SealProductRepository;
import com.yfive.gbjs.domain.seal.repository.SealRepository;
import com.yfive.gbjs.domain.seal.repository.UserSealRepository;
import com.yfive.gbjs.domain.user.entity.User;
import com.yfive.gbjs.domain.user.service.UserService;
import com.yfive.gbjs.global.error.exception.CustomException;
import com.yfive.gbjs.global.s3.entity.PathName;
import com.yfive.gbjs.global.s3.service.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 띠부씰 서비스 구현체 띠부씰 관련 비즈니스 로직을 구현 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SealServiceImpl implements SealService {

  private final SealRepository sealRepository;
  private final UserSealRepository userSealRepository;
  private final SealProductRepository sealProductRepository;
  private final SealProductConverter sealProductConverter;
  private final SealConverter sealConverter;
  private final UserSealConverter userSealConverter;
  private final UserService userService;
  private final S3Service s3Service;

  /** ID로 특정 띠부씰을 조회하여 반환 */
  @Override
  public SealResponse.SealDTO getSealById(Long sealId) {
    Seal seal =
        sealRepository
            .findById(sealId)
            .orElseThrow(() -> new CustomException(SealErrorStatus.SEAL_NOT_FOUND));
    return sealConverter.toDTO(seal);
  }

  /** ID로 특정 띠부씰을 조회하여 반환 */
  @Override
  public SealResponse.SealDTO searchSeals(Long sealSpotId) {
    Seal seal =
        sealRepository
            .findBySealSpotId(sealSpotId)
            .orElseThrow(() -> new CustomException(SealErrorStatus.SEAL_NOT_FOUND));

    return sealConverter.toDTO(seal);
  }

  /** 등록된 모든 띠부씰을 조회하여 반환 */
  @Override
  public SealResponse.SealListDTO getAllSeals(SortBy sortBy) {
    List<Seal> seals = sealRepository.findAll();

    // 정렬 적용
    List<SealResponse.SealDTO> sealDTOs =
        seals.stream()
            .map(sealConverter::toDTO)
            .sorted(getSealComparator(sortBy))
            .collect(Collectors.toList());

    return sealConverter.toListDTO(sealDTOs);
  }

  /** 특정 사용자의 띠부씰 수집 현황을 조회 모든 띠부씰에 대해 사용자의 수집 여부와 수집 시간을 포함하여 반환 */
  @Override
  public UserSealResponse.UserSealListDTO getUserSeals(SortBy sortBy) {
    Long userId = userService.getCurrentUser().getId();
    List<Seal> allSeals = sealRepository.findAll();
    List<UserSeal> userSeals = userSealRepository.findByUserId(userId);

    // 사용자가 수집한 띠부씰을 Map으로 변환 (빠른 조회를 위해)
    Map<Long, UserSeal> userSealMap =
        userSeals.stream().collect(Collectors.toMap(us -> us.getSeal().getId(), us -> us));

    // 모든 띠부씰에 대해 사용자의 수집 정보를 합쳐서 반환
    List<UserSealResponse.UserSealDTO> userSealDTOs =
        allSeals.stream()
            .map(
                seal -> {
                  UserSeal userSeal = userSealMap.get(seal.getId());
                  boolean collected = userSeal != null && userSeal.getCollected();
                  LocalDateTime collectedAt = userSeal != null ? userSeal.getCollectedAt() : null;
                  return userSealConverter.toDTO(seal, collected, collectedAt);
                })
            .sorted(getUserSealComparator(sortBy))
            .collect(Collectors.toList());

    return userSealConverter.toListDTO(userSealDTOs);
  }

  /** 특정 사용자의 띠부씰 수집 개수를 조회 */
  @Override
  public UserSealResponse.SealCountResponseDTO getSealCounts() {
    Long userId = userService.getCurrentUser().getId();
    long totalCount = sealRepository.count();
    long collectedCount = userSealRepository.countByUserId(userId);

    return UserSealResponse.SealCountResponseDTO.builder()
        .totalCount(totalCount)
        .collectedCount(collectedCount)
        .build();
  }

  /** 띠부씰 이미지, 시 등록(개발용) */
  @Override
  @Transactional
  public SealResponse.SealDTO uploadSealImages(
      Long sealId, MultipartFile frontImage, MultipartFile backImage, String content) {
    Seal seal =
        sealRepository
            .findById(sealId)
            .orElseThrow(() -> new CustomException(SealErrorStatus.SEAL_NOT_FOUND));

    if (frontImage != null && !frontImage.isEmpty()) {
      String frontImageUrl = s3Service.uploadFile(PathName.SEAL, frontImage);
      seal.setFrontImageUrl(frontImageUrl);
    }

    if (backImage != null && !backImage.isEmpty()) {
      String backImageUrl = s3Service.uploadFile(PathName.SEAL, backImage);
      seal.setBackImageUrl(backImageUrl);
    }

    if (content != null && !content.trim().isEmpty()) {
      seal.setContent(content);
    }

    return sealConverter.toDTO(seal);
  }

  /** 등록된 모든 띠부씰 상품을 조회하여 반환 */
  @Override
  public SealProductResponse.SealProductListDTO getSealProducts() {
    List<com.yfive.gbjs.domain.seal.entity.SealProduct> products = sealProductRepository.findAll();
    List<SealProductResponse.SealProductDTO> productDTOs =
        products.stream().map(sealProductConverter::toDTO).collect(Collectors.toList());

    return sealProductConverter.toListDTO(productDTOs);
  }

  /** Seal 정렬을 위한 Comparator 생성 */
  private java.util.Comparator<SealResponse.SealDTO> getSealComparator(SortBy sortBy) {
    switch (sortBy) {
      case RARITY:
        // 희귀한 순서대로 정렬 (RED > GREEN > BLUE), 같은 희귀도면 번호순
        return java.util.Comparator.comparing(
                (SealResponse.SealDTO s) -> getRarityOrder(s.getRarity()))
            .thenComparing(SealResponse.SealDTO::getNumber);
      case LOCATION:
        // 지역명대로 정렬, 같은 지역명이면 번호순
        return java.util.Comparator.comparing(SealResponse.SealDTO::getLocationName)
            .thenComparing(SealResponse.SealDTO::getNumber);
      case NUMBER:
      default:
        // 번호순: 1번부터 정렬 (기본값)
        return java.util.Comparator.comparing(SealResponse.SealDTO::getNumber);
    }
  }

  /** UserSeal 정렬을 위한 Comparator 생성 */
  private java.util.Comparator<UserSealResponse.UserSealDTO> getUserSealComparator(SortBy sortBy) {
    switch (sortBy) {
      case RARITY:
        // 희귀한 순서대로 정렬 (RED > GREEN > BLUE), 같은 희귀도면 번호순
        return java.util.Comparator.comparing(
                (UserSealResponse.UserSealDTO s) -> getRarityOrder(s.getRarity()))
            .thenComparing(UserSealResponse.UserSealDTO::getNumber);
      case COLLECTED:
        // 먼저 수집한 상품부터 보여주기 (수집한 것 우선, 수집 시간 오름차순)
        return java.util.Comparator.comparing(UserSealResponse.UserSealDTO::isCollected)
            .reversed() // 수집한 것 먼저
            .thenComparing(
                (UserSealResponse.UserSealDTO s) -> {
                  // 수집되지 않은 경우 MAX 값으로 처리하여 뒤로 보냄
                  if (s.getCollectedAt() == null) {
                    return java.time.LocalDateTime.MAX;
                  }
                  return s.getCollectedAt();
                }) // 수집 시간 오름차순
            .thenComparing(UserSealResponse.UserSealDTO::getNumber); // 같은 시간이면 번호순
      case LOCATION:
        // 지역명대로 정렬, 같은 지역명이면 번호순
        return java.util.Comparator.comparing(UserSealResponse.UserSealDTO::getLocationName)
            .thenComparing(UserSealResponse.UserSealDTO::getNumber);
      case NUMBER:
      default:
        // 번호순: 1번부터 정렬 (기본값)
        return java.util.Comparator.comparing(UserSealResponse.UserSealDTO::getNumber);
    }
  }

  /** 희귀도 정렬 순서 정의 (RED(희귀) > GREEN(보통) > BLUE(흔함)) */
  private int getRarityOrder(Rarity rarity) {
    if (rarity == Rarity.RED) {
      return 1; // 희귀 (빨간색)
    } else if (rarity == Rarity.GREEN) {
      return 2; // 보통 (초록색)
    } else if (rarity == Rarity.BLUE) {
      return 3; // 흔함 (파란색)
    } else {
      return 4;
    }
  }

  /** 현재 위치 기반 가까운 띠부씰 조회 */
  @Override
  public SealResponse.NearbySealListDTO getNearbySeals(Double latitude, Double longitude) {
    // 모든 Seal 조회 (SealSpot과 AudioGuide 정보 포함)
    List<Seal> allSeals = sealRepository.findAll();

    // 각 Seal과의 거리를 계산하여 DTO 리스트 생성
    List<SealResponse.NearbySealDTO> nearbySealDTOs =
        allSeals.stream()
            .filter(
                seal -> seal.getSealSpot() != null && seal.getSealSpot().getAudioGuide() != null)
            .map(
                seal -> {
                  // AudioGuide에서 위도/경도 가져오기
                  String guideLatStr = seal.getSealSpot().getAudioGuide().getLatitude();
                  String guideLonStr = seal.getSealSpot().getAudioGuide().getLongitude();

                  if (guideLatStr == null || guideLonStr == null) {
                    return null;
                  }

                  try {
                    double guideLat = Double.parseDouble(guideLatStr);
                    double guideLon = Double.parseDouble(guideLonStr);

                    // 거리 계산 (Haversine formula) - km를 m로 변환
                    double distanceKm = calculateDistance(latitude, longitude, guideLat, guideLon);
                    int distanceM = (int) Math.round(distanceKm * 1000);

                    return SealResponse.NearbySealDTO.builder()
                        .sealId(seal.getId())
                        .number(seal.getNumber())
                        .rarity(seal.getRarity())
                        .frontImageUrl(seal.getFrontImageUrl())
                        .spotName(seal.getSealSpot().getName())
                        .locationName(seal.getSealSpot().getLocation().name())
                        .latitude(guideLat)
                        .longitude(guideLon)
                        .distance(distanceM)
                        .build();
                  } catch (NumberFormatException e) {
                    return null;
                  }
                })
            .filter(dto -> dto != null)
            .sorted(
                java.util.Comparator.comparing(
                    (SealResponse.NearbySealDTO dto) -> dto.getDistance()))
            .limit(4) // 가장 가까운 4개만
            .collect(Collectors.toList());

    return SealResponse.NearbySealListDTO.builder().nearbySeals(nearbySealDTOs).build();
  }

  /**
   * Haversine 공식을 사용한 두 지점 간 거리 계산
   *
   * @param lat1 위도1
   * @param lon1 경도1
   * @param lat2 위도2
   * @param lon2 경도2
   * @return 거리 (km)
   */
  private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    final double R = 6371; // 지구 반지름 (km)

    double latDistance = Math.toRadians(lat2 - lat1);
    double lonDistance = Math.toRadians(lon2 - lon1);

    double a =
        Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2)
                * Math.sin(lonDistance / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double distance = R * c;

    // 소수점 2자리까지 반올림
    return Math.round(distance * 100.0) / 100.0;
  }

  /** 위치 인증을 통한 띠부씰 획득 */
  @Override
  @Transactional
  public SealResponse.CollectSealResultDTO collectSeal(
      Long sealId, Double latitude, Double longitude) {
    // 1. 띠부씰 조회
    Seal seal =
        sealRepository
            .findById(sealId)
            .orElseThrow(() -> new CustomException(SealErrorStatus.SEAL_NOT_FOUND));

    // 2. SealSpot과 AudioGuide 확인
    if (seal.getSealSpot() == null || seal.getSealSpot().getAudioGuide() == null) {
      throw new CustomException(SealErrorStatus.SEAL_LOCATION_INFO_MISSING);
    }

    // 3. AudioGuide에서 위도/경도 가져오기
    String guideLatStr = seal.getSealSpot().getAudioGuide().getLatitude();
    String guideLonStr = seal.getSealSpot().getAudioGuide().getLongitude();

    if (guideLatStr == null || guideLonStr == null) {
      throw new CustomException(SealErrorStatus.SEAL_LOCATION_INFO_MISSING);
    }

    try {
      double guideLat = Double.parseDouble(guideLatStr);
      double guideLon = Double.parseDouble(guideLonStr);

      // 4. 거리 계산 (km를 m로 변환)
      double distanceKm = calculateDistance(latitude, longitude, guideLat, guideLon);
      int distanceM = (int) Math.round(distanceKm * 1000);

      // 5. 지역별 허용 반경 내인지 확인 (울릉도, 독도는 2km, 나머지는 500m)
      // ULLUNG location에는 울릉도와 독도가 모두 포함됨
      boolean isUllung = seal.getSealSpot().getLocation() == Location.ULLUNG;
      int allowedRadius = isUllung ? 2000 : 500;

      if (distanceM > allowedRadius) {
        // 울릉도/독도는 2km, 나머지는 500m 메시지 구분
        throw new CustomException(
            isUllung ? SealErrorStatus.SEAL_TOO_FAR_ULLUNG : SealErrorStatus.SEAL_TOO_FAR_GENERAL);
      }

      // 6. 현재 사용자 조회
      User currentUser = userService.getCurrentUser();
      Long userId = currentUser.getId();

      // 7. 이미 획득했는지 확인
      boolean alreadyCollected = userSealRepository.existsByUser_IdAndSeal_Id(userId, sealId);
      if (alreadyCollected) {
        throw new CustomException(SealErrorStatus.SEAL_ALREADY_COLLECTED);
      }

      // 8. UserSeal 생성 및 저장
      UserSeal userSeal =
          UserSeal.builder()
              .user(currentUser)
              .seal(seal)
              .collected(true)
              .collectedAt(LocalDateTime.now())
              .build();

      userSealRepository.save(userSeal);

      log.info("띠부씰 획득 성공! userId: {}, sealId: {}, 거리: {}m", userId, sealId, distanceM);

      return SealResponse.CollectSealResultDTO.builder()
          .id(sealId)
          .success(true)
          .distance(distanceM)
          .build();

    } catch (NumberFormatException e) {
      throw new CustomException(SealErrorStatus.SEAL_LOCATION_INFO_MISSING);
    }
  }

  /** 띠부씰 획득 실패 메시지 조회 */
  @Override
  public String getFailureMessage(Long sealId) {
    try {
      Seal seal = sealRepository.findById(sealId).orElse(null);
      if (seal == null || seal.getSealSpot() == null) {
        return "띠부씰 획득에 실패했습니다.";
      }

      boolean isUllung = seal.getSealSpot().getLocation() == Location.ULLUNG;
      return isUllung ? "띠부씰 획득에 실패했습니다. 2km 이내로 가까이 가주세요." : "띠부씰 획득에 실패했습니다. 500m 이내로 가까이 가주세요.";
    } catch (Exception e) {
      return "띠부씰 획득에 실패했습니다.";
    }
  }

  /** 획득한 띠부씰 삭제 */
  @Override
  @Transactional
  public void deleteCollectedSeal(Long sealId) {
    Long userId = userService.getCurrentUser().getId();

    // UserSeal 조회
    UserSeal userSeal =
        userSealRepository
            .findByUser_IdAndSeal_Id(userId, sealId)
            .orElseThrow(() -> new CustomException(SealErrorStatus.USER_SEAL_NOT_FOUND));

    // UserSeal 삭제
    userSealRepository.delete(userSeal);
  }
}
