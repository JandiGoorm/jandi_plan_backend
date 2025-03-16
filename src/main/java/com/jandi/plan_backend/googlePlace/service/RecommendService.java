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
     * 최근 30일 이내에 저장된 맛집 추천 데이터가 있으면 그 개수를 확인한다.
     * - 10개 이상이면 그대로 반환
     * - 10개 미만이면 Google Places API를 호출하여 부족분을 채운 뒤 반환
     */
    public List<RecommPlaceRespDTO> getAllRecommendedPlace(RecommPlaceReqDTO reqDTO) {
        // cityId 유효성 검증
        Integer cityId = reqDTO.getCityId();
        City cityEntity = cityRepo.findById(cityId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid cityId: " + cityId + " (City not found in DB)"
                ));

        String countryName = cityEntity.getCountry().getName();
        String cityName = cityEntity.getName();

        // 최근 30일 이내 데이터 조회
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(30);
        List<PlaceRecommendation> recentList =
                placeRepo.findByCountryAndCityAndCreatedAtAfter(countryName, cityName, oneMonthAgo);

        if (recentList.isEmpty()) {
            // 완전히 0개인 경우: 기존 데이터를 모두 지우고 새로 검색
            log.info("No recent data found for cityId={}. Deleting old data and fetching new data.", cityId);
            placeRepo.deleteByCountryAndCity(countryName, cityName);

            // 새로 가져와서 DB 저장
            callGooglePlacesApiAndAppend(countryName, cityName, 0);
        } else {
            // 1~9개인 경우: 기존 데이터는 유지하고, 부족분만큼 추가로 검색
            if (recentList.size() < 10) {
                log.info("Found {} recent data for cityId={}, fetching more to reach 10...", recentList.size(), cityId);
                callGooglePlacesApiAndAppend(countryName, cityName, recentList.size());
            }
        }

        // 최종적으로 DB에서 10개까지만 추려서 반환
        List<PlaceRecommendation> finalList = placeRepo.findByCountryAndCityAndCreatedAtAfter(countryName, cityName, oneMonthAgo);
        return finalList.stream()
                .map(this::convertToDTO)
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * DB에 저장된 개수가 countSoFar(기존 개수)보다 적을 경우,
     * Google Places API의 nextPageToken을 사용해 추가로 검색을 시도한다.
     */
    private void callGooglePlacesApiAndAppend(String country, String city, int countSoFar) {
        String searchKeyword = country + " " + city + " 맛집";
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(googleApiKey)
                .build();

        try {
            // 첫 페이지 검색
            PlacesSearchResponse response = PlacesApi.textSearchQuery(context, searchKeyword).await();
            saveSearchResultsToDB(response.results, country, city);

            // nextPageToken
            String nextPageToken = response.nextPageToken;

            // DB에 저장된 개수를 다시 확인
            int storedCount = placeRepo.findByCountryAndCity(country, city).size();

            // DB에 10개 미만이고, 다음 페이지가 존재하면 계속 조회
            while (storedCount < 10 && nextPageToken != null) {
                // next_page_token 유효해질 때까지 2초 대기
                Thread.sleep(2000);

                response = PlacesApi.textSearchQuery(context, searchKeyword)
                        .pageToken(nextPageToken)
                        .await();

                saveSearchResultsToDB(response.results, country, city);

                // 다음 페이지 토큰 갱신
                nextPageToken = response.nextPageToken;

                // 다시 DB에 저장된 개수를 확인
                storedCount = placeRepo.findByCountryAndCity(country, city).size();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GoogleApiException("Thread interrupted while fetching next page", e);
        } catch (Exception e) {
            throw new GoogleApiException(e);
        }
    }

    /**
     * PlacesSearchResult 배열을 순회하며 DB에 저장하는 메서드
     */
    private void saveSearchResultsToDB(PlacesSearchResult[] results, String country, String city) {
        if (results == null) {
            return;
        }
        for (PlacesSearchResult sr : results) {
            getPlaceDetailsAndSave(sr.placeId, country, city);
        }
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
