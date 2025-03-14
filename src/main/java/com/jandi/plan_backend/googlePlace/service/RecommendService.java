package com.jandi.plan_backend.googlePlace.service;

import com.jandi.plan_backend.googlePlace.dto.RecommPlaceReqDTO;
import com.jandi.plan_backend.googlePlace.dto.RecommPlaceRespDTO;
import com.jandi.plan_backend.googlePlace.entity.PlaceRecommendation;
import com.jandi.plan_backend.googlePlace.repository.PlaceRecommendationRepository;
import com.jandi.plan_backend.util.service.GoogleApiException;
import com.jandi.plan_backend.user.entity.City;
import com.jandi.plan_backend.user.repository.CityRepository;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecommendService {

    @Value("${google.api.key}")
    private String googleApiKey;

    private final PlaceRecommendationRepository placeRepo;
    private final CityRepository cityRepo;

    public RecommendService(PlaceRecommendationRepository placeRepo,
                            CityRepository cityRepo) {
        this.placeRepo = placeRepo;
        this.cityRepo = cityRepo;
    }

    /**
     * 요청받은 cityId로 City 엔티티를 조회한 뒤,
     * 최근 30일 이내에 저장된 맛집 추천 데이터가 있으면 그대로 반환,
     * 없으면 Google Places API로 새 데이터를 받아와 DB에 저장 후 반환.
     */
    public List<RecommPlaceRespDTO> getAllRecommendedPlace(RecommPlaceReqDTO reqDTO) {
        // cityId가 유효한지(= DB에 존재하는지) 확인
        Integer cityId = reqDTO.getCityId();
        City cityEntity = cityRepo.findById(cityId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid cityId: " + cityId + " (City not found in DB)"
                ));

        // 도시 이름과 국가 이름 가져오기
        // (Country 엔티티에 getName()이 있다고 가정)
        String countryName = cityEntity.getCountry().getName();
        String cityName = cityEntity.getName();

        // 최근 30일 이내 데이터 조회
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(30);
        List<PlaceRecommendation> recentList =
                placeRepo.findByCountryAndCityAndCreatedAtAfter(countryName, cityName, oneMonthAgo);

        if (!recentList.isEmpty()) {
            log.info("Returning data from DB for cityId={}, size={}", cityId, recentList.size());
            return recentList.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        log.info("No recent data found for cityId={}. Deleting old data and fetching new data.", cityId);
        // 기존 데이터 삭제
        placeRepo.deleteByCountryAndCity(countryName, cityName);

        // Google Places API로 새 데이터 받아오기
        return callGooglePlacesApiAndSave(countryName, cityName);
    }

    /**
     * Google Places API를 호출하여 "국가명 + 도시명 + '맛집'" 으로 검색 후
     * 결과를 DB에 저장하고 DTO 리스트로 반환
     */
    private List<RecommPlaceRespDTO> callGooglePlacesApiAndSave(String country, String city) {
        String searchKeyword = country + " " + city + " 맛집";
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(googleApiKey)
                .build();

        List<RecommPlaceRespDTO> result = new ArrayList<>();
        try {
            PlacesSearchResponse response = PlacesApi.textSearchQuery(context, searchKeyword).await();
            if (response.results == null || response.results.length == 0) {
                log.info("No place found for keyword={}", searchKeyword);
                return Collections.emptyList();
            }

            PlacesSearchResult[] searchResults = response.results;
            for (int i = 0; i < Math.min(searchResults.length, 10); i++) {
                PlacesSearchResult sr = searchResults[i];
                RecommPlaceRespDTO dto = getPlaceDetailsAndSave(sr.placeId, country, city);
                if (dto != null) {
                    result.add(dto);
                }
            }
        } catch (Exception e) {
            throw new GoogleApiException(e);
        }
        return result;
    }

    /**
     * Google Places API에서 placeId로 상세 정보를 가져온 뒤 DB에 저장.
     * 이미 DB에 placeId가 존재하면 기존 데이터를 반환(중복 저장 방지).
     */
    private RecommPlaceRespDTO getPlaceDetailsAndSave(String placeId, String country, String city) {
        Optional<PlaceRecommendation> existing = placeRepo.findByPlaceId(placeId);
        if (existing.isPresent()) {
            return convertToDTO(existing.get());
        }

        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(googleApiKey)
                .disableRetries()
                .build();

        try {
            PlaceDetails details = PlacesApi.placeDetails(context, placeId)
                    .language("ko")
                    .await();

            float rating = details.rating;
            int ratingCount = details.userRatingsTotal;

            String photoUrl = null;
            if (details.photos != null && details.photos.length > 0) {
                photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=500&photoreference="
                        + details.photos[0].photoReference
                        + "&key=" + googleApiKey;
            }

            String openTimeJson = null;
            if (details.currentOpeningHours != null && details.currentOpeningHours.weekdayText != null) {
                openTimeJson = String.join(", ", details.currentOpeningHours.weekdayText);
            }

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

            placeRepo.save(entity);
            return convertToDTO(entity);
        } catch (Exception e) {
            log.error("Error fetching details for placeId {}: {}", placeId, e.getMessage());
            return null;
        }
    }

    /**
     * PlaceRecommendation 엔티티를 RecommPlaceRespDTO로 변환
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
