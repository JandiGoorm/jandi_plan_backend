package com.jandi.plan_backend.googlePlace.service;

import com.google.gson.Gson;
import com.google.maps.GeoApiContext;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.PlacesApi;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.jandi.plan_backend.googlePlace.dto.RecommPlaceReqDTO;
import com.jandi.plan_backend.googlePlace.dto.RecommPlaceRespDTO;
import com.jandi.plan_backend.googlePlace.entity.PlaceRecommendation;
import com.jandi.plan_backend.googlePlace.repository.PlaceRecommendationRepository;
import com.jandi.plan_backend.util.service.GoogleApiException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class RecommendService {

    @Value("${google.api.key}")
    private String googleApiKey;

    private final PlaceRecommendationRepository placeRecommendationRepository;
    private final Gson gson = new Gson(); // openTime -> JSON 변환용

    public RecommendService(PlaceRecommendationRepository placeRecommendationRepository) {
        this.placeRecommendationRepository = placeRecommendationRepository;
    }

    /**
     * 한 번에 최대 10개씩만 Place Details를 가져오기 위해
     * 검색 결과(최대 20~30개) 중 상위 10개만 가져온다거나,
     * 혹은 파라미터로 pageSize를 받아 조절할 수 있음.
     */
    @Transactional
    public List<RecommPlaceRespDTO> getAllRecommendedPlace(RecommPlaceReqDTO reqDTO) {
        List<RecommPlaceRespDTO> resultList = new ArrayList<>();

        String country = reqDTO.getCountry();
        String city = reqDTO.getCity();
        String searchKeyword = country + " " + city + " 맛집";

        // Google Maps API 컨텍스트 생성
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(googleApiKey)
                .build();

        try {
            // Text Search
            PlacesSearchResponse response = PlacesApi.textSearchQuery(context, searchKeyword).await();
            if (response.results != null && response.results.length > 0) {
                // 상위 10개만 처리 (원하면 더 줄일 수도 있음)
                int limit = Math.min(response.results.length, 10);
                for (int i = 0; i < limit; i++) {
                    PlacesSearchResult psr = response.results[i];
                    RecommPlaceRespDTO placeDTO = getPlaceDetails(psr.placeId, country, city);
                    if (placeDTO != null) {
                        resultList.add(placeDTO);
                    }
                }
            } else {
                log.info("No place found for keyword: {}", searchKeyword);
            }
        } catch (Exception e) {
            // GoogleApiException(String message, Throwable cause) 사용
            throw new GoogleApiException("Error occurred while calling Google Places API: " + e.getMessage(), e);
        }
        return resultList;
    }

    /**
     * Place Details 호출
     * secondaryOpeningHours를 무시하기 위해 fields()를 명시
     */
    private RecommPlaceRespDTO getPlaceDetails(String placeId, String country, String city) {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(googleApiKey)
                .disableRetries()
                .build();

        try {
            PlaceDetails details = PlacesApi.placeDetails(context, placeId)
                    .language("ko")
                    .fields(
                            PlaceDetailsRequest.FieldMask.NAME,
                            PlaceDetailsRequest.FieldMask.PLACE_ID,
                            PlaceDetailsRequest.FieldMask.GEOMETRY,
                            PlaceDetailsRequest.FieldMask.FORMATTED_ADDRESS,
                            PlaceDetailsRequest.FieldMask.OPENING_HOURS,
                            PlaceDetailsRequest.FieldMask.CURRENT_OPENING_HOURS,
                            PlaceDetailsRequest.FieldMask.RATING,
                            PlaceDetailsRequest.FieldMask.USER_RATINGS_TOTAL,
                            PlaceDetailsRequest.FieldMask.URL,
                            PlaceDetailsRequest.FieldMask.PHOTOS,
                            PlaceDetailsRequest.FieldMask.DINE_IN
                    )
                    .await();

            // photos가 없는 경우 null 반환
            if (details.photos == null || details.photos.length == 0) {
                return null;
            }

            // photoUrl 생성
            String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=500&photoreference="
                    + details.photos[0].photoReference
                    + "&key="
                    + googleApiKey;

            // weekdayText 추출
            String[] weekdayText = null;
            if (details.currentOpeningHours != null && details.currentOpeningHours.weekdayText != null) {
                weekdayText = details.currentOpeningHours.weekdayText;
            } else if (details.openingHours != null && details.openingHours.weekdayText != null) {
                weekdayText = details.openingHours.weekdayText;
            }

            Map<String, String> openTimeMap = (weekdayText == null)
                    ? new LinkedHashMap<>()
                    : convertToMap(weekdayText);

            // DB 저장용 엔티티가 이미 있는지 확인
            Optional<PlaceRecommendation> existing = placeRecommendationRepository.findByPlaceId(placeId);
            PlaceRecommendation entity = existing.orElseGet(PlaceRecommendation::new);

            // float, int 기본형이므로 null 체크 불필요
            float ratingVal = details.rating;  // 없는 경우 0.0
            int ratingCountVal = details.userRatingsTotal;  // 없는 경우 0

            // 엔티티 설정
            entity.setPlaceId(details.placeId);
            entity.setName(details.name);
            entity.setCountry(country);
            entity.setCity(city);
            entity.setAddress(details.formattedAddress);
            entity.setDetailUrl((details.url == null) ? null : details.url.toString());
            entity.setPhotoUrl(photoUrl);
            entity.setLatitude(details.geometry.location.lat);
            entity.setLongitude(details.geometry.location.lng);
            entity.setRating(Math.round(ratingVal * 100) / 100.0); // 소수점 둘째 자리
            entity.setRatingCount(ratingCountVal);
            entity.setDineIn(details.dineIn != null && details.dineIn);

            // open_time_json
            String openTimeJson = gson.toJson(openTimeMap);
            entity.setOpenTimeJson(openTimeJson);

            placeRecommendationRepository.save(entity);

            // DTO로 변환
            RecommPlaceRespDTO dto = new RecommPlaceRespDTO();
            dto.setPlaceId(entity.getPlaceId());
            dto.setName(entity.getName());
            dto.setCountry(entity.getCountry());
            dto.setCity(entity.getCity());
            dto.setAddress(entity.getAddress());
            dto.setUrl(entity.getDetailUrl());
            dto.setPhotoUrl(entity.getPhotoUrl());
            dto.setLatitude(entity.getLatitude());
            dto.setLongitude(entity.getLongitude());
            dto.setRating(entity.getRating() == null ? 0.0 : entity.getRating());
            dto.setRatingCount(entity.getRatingCount() == null ? 0 : entity.getRatingCount());
            dto.setDineIn(Boolean.TRUE.equals(entity.getDineIn()));
            dto.setOpenTimeJson(entity.getOpenTimeJson());

            return dto;

        } catch (Exception e) {
            log.error("Error fetching place details for placeId {}: {}", placeId, e.getMessage());
            return null; // 실패 시 null
        }
    }

    /**
     * String[] → Map<String, String> 변환
     * "월요일: 영업 12:00~20:00" 같은 포맷을 요일별로 저장
     */
    public static Map<String, String> convertToMap(String[] scheduleArray) {
        Map<String, String> scheduleMap = new LinkedHashMap<>();
        // 단순 파싱 로직: "월요일: 10:00~21:00", "화요일: 10:00~21:00" ...
        for (String line : scheduleArray) {
            int idx = line.indexOf(":");
            if (idx > 0) {
                String day = line.substring(0, idx).trim(); // "월요일"
                String time = line.substring(idx + 1).trim(); // "10:00~21:00"
                scheduleMap.put(day, time);
            } else {
                // ':'가 없으면 통째로 저장
                scheduleMap.put(line, "");
            }
        }
        return scheduleMap;
    }
}
