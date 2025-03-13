package com.jandi.plan_backend.googlePlace.service;

import com.jandi.plan_backend.googlePlace.dto.RecommPlaceReqDTO;
import com.jandi.plan_backend.googlePlace.dto.RecommPlaceRespDTO;
import com.jandi.plan_backend.googlePlace.entity.PlaceRecommendation;
import com.jandi.plan_backend.googlePlace.repository.PlaceRecommendationRepository;
import com.jandi.plan_backend.util.service.GoogleApiException;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecommendService {

    @Value("${google.api.key}")
    private String googleApiKey;

    private final PlaceRecommendationRepository placeRepo;

    public RecommendService(PlaceRecommendationRepository placeRepo) {
        this.placeRepo = placeRepo;
    }

    /**
     * 추천 장소 목록을 가져옵니다.
     *
     * 1. DB에 저장된 데이터가 최근 30일 이내에 있다면 그대로 반환합니다.
     * 2. 그렇지 않으면 기존 데이터를 삭제하고 Google Places API를 호출하여 새로운 데이터를 저장 후 반환합니다.
     *
     * @param reqDTO 요청 DTO (국가, 도시 정보 포함)
     * @return 추천 장소의 DTO 리스트
     */
    public List<RecommPlaceRespDTO> getAllRecommendedPlace(RecommPlaceReqDTO reqDTO) {
        String country = reqDTO.getCountry();
        String city = reqDTO.getCity();

        // 30일 전 시점을 계산 (최근 데이터 확인)
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(30);
        // DB에서 country, city 기준으로 30일 이후에 생성된 데이터 조회
        List<PlaceRecommendation> recentList = placeRepo.findByCountryAndCityAndCreatedAtAfter(country, city, oneMonthAgo);

        if (!recentList.isEmpty()) {
            log.info("Returning data from DB for country={}, city={}, size={}", country, city, recentList.size());
            // 조회된 데이터를 DTO로 변환하여 반환
            return recentList.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        log.info("No recent data found for country={}, city={}. Deleting old data and fetching new data.", country, city);
        // 최근 데이터가 없으면 기존 데이터를 삭제
        placeRepo.deleteByCountryAndCity(country, city);
        // Google Places API를 호출하여 데이터를 새로 받아오고 저장한 후 반환
        return callGooglePlacesApiAndSave(country, city);
    }

    /**
     * Google Places API를 호출하여 맛집 데이터를 받아오고 DB에 저장한 후 DTO 리스트로 반환합니다.
     *
     * @param country 국가
     * @param city 도시
     * @return 추천 장소의 DTO 리스트
     */
    private List<RecommPlaceRespDTO> callGooglePlacesApiAndSave(String country, String city) {
        // 검색 키워드 생성: "국가 도시 맛집"
        String searchKeyword = country + " " + city + " 맛집";
        GeoApiContext context = new GeoApiContext.Builder().apiKey(googleApiKey).build();
        List<RecommPlaceRespDTO> result = new ArrayList<>();

        try {
            // Google Places API의 textSearchQuery 메서드 호출
            PlacesSearchResponse response = PlacesApi.textSearchQuery(context, searchKeyword).await();
            if (response.results == null || response.results.length == 0) {
                log.info("No place found for keyword={}", searchKeyword);
                return Collections.emptyList();
            }
            PlacesSearchResult[] searchResults = response.results;
            // 최대 10개의 결과만 처리 (예시 제한)
            for (int i = 0; i < Math.min(searchResults.length, 10); i++) {
                PlacesSearchResult sr = searchResults[i];
                // 각 장소의 상세 정보를 가져와 DB 저장 및 DTO 변환
                RecommPlaceRespDTO dto = getPlaceDetailsAndSave(sr.placeId, country, city);
                if (dto != null) {
                    result.add(dto);
                }
            }
        } catch (Exception e) {
            // 예외 발생 시 GoogleApiException으로 감싸서 던짐
            throw new GoogleApiException(e);
        }
        return result;
    }

    /**
     * 특정 placeId에 대한 상세 정보를 Google Places API에서 받아와 DB에 저장한 후 DTO로 변환합니다.
     *
     * @param placeId Google Place의 고유 ID
     * @param country 국가
     * @param city 도시
     * @return 추천 장소 DTO (저장 실패 시 null)
     */
    private RecommPlaceRespDTO getPlaceDetailsAndSave(String placeId, String country, String city) {
        // DB에 이미 해당 placeId가 저장되어 있는지 확인 (중복 저장 방지)
        Optional<PlaceRecommendation> existing = placeRepo.findByPlaceId(placeId);
        if (existing.isPresent()) {
            return convertToDTO(existing.get());
        }

        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(googleApiKey)
                .disableRetries()
                .build();
        try {
            // Google Places API로 상세 정보 조회 (한국어로 요청)
            PlaceDetails details = PlacesApi.placeDetails(context, placeId)
                    .language("ko")
                    .await();

            // Google Places API의 rating과 userRatingsTotal은 기본값이 있으므로 바로 할당
            float rating = details.rating;
            int ratingCount = details.userRatingsTotal;

            String photoUrl = null;
            // 사진이 있을 경우 첫 번째 사진의 URL 생성
            if (details.photos != null && details.photos.length > 0) {
                photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=500&photoreference="
                        + details.photos[0].photoReference + "&key=" + googleApiKey;
            }
            String openTimeJson = null;
            // 영업시간 정보가 존재하면 문자열 형태로 변환
            if (details.currentOpeningHours != null && details.currentOpeningHours.weekdayText != null) {
                openTimeJson = String.join(", ", details.currentOpeningHours.weekdayText);
            }

            // 새로운 PlaceRecommendation 엔티티 생성 및 값 설정
            PlaceRecommendation entity = new PlaceRecommendation();
            entity.setPlaceId(placeId);
            entity.setName(details.name);
            entity.setDetailUrl(details.url != null ? details.url.toString() : null);
            entity.setRating(rating);
            entity.setPhotoUrl(photoUrl);
            entity.setAddress(details.formattedAddress);
            entity.setLatitude(details.geometry.location.lat);
            entity.setLongitude(details.geometry.location.lng);
            entity.setRatingCount(ratingCount);
            entity.setDineIn(details.dineIn);
            entity.setOpenTimeJson(openTimeJson);
            entity.setCountry(country);
            entity.setCity(city);

            // DB에 엔티티 저장
            placeRepo.save(entity);
            return convertToDTO(entity);
        } catch (Exception e) {
            log.error("Error fetching details for placeId {}: {}", placeId, e.getMessage());
            return null;
        }
    }

    /**
     * PlaceRecommendation 엔티티를 RecommPlaceRespDTO로 변환합니다.
     *
     * @param entity DB 엔티티
     * @return 변환된 DTO
     */
    private RecommPlaceRespDTO convertToDTO(PlaceRecommendation entity) {
        RecommPlaceRespDTO dto = new RecommPlaceRespDTO();
        dto.setPlaceId(entity.getPlaceId());
        dto.setName(entity.getName());
        dto.setUrl(entity.getDetailUrl());
        dto.setRating(entity.getRating());
        dto.setPhotoUrl(entity.getPhotoUrl());
        dto.setAddress(entity.getAddress());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setRatingCount(entity.getRatingCount());
        dto.setDineIn(entity.isDineIn());
        dto.setOpenTimeJson(entity.getOpenTimeJson());
        dto.setCountry(entity.getCountry());
        dto.setCity(entity.getCity());
        return dto;
    }
}
