package com.jandi.plan_backend.googlePlace.service;

import org.springframework.beans.factory.annotation.Value;
import com.jandi.plan_backend.googlePlace.dto.RecommPlaceReqDTO;
import com.jandi.plan_backend.googlePlace.dto.RecommPlaceRespDTO;
import com.jandi.plan_backend.util.service.GoogleApiException;
import lombok.extern.slf4j.Slf4j;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.google.maps.model.PlaceDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RecommendService {

    @Value("${google.api.key}")
    private String googleApiKey;

    // 추천 맛집 검색 결과 반환
    public List<RecommPlaceRespDTO> getAllRecommendedPlace(RecommPlaceReqDTO reqDTO) throws GoogleApiException {
        List<RecommPlaceRespDTO> result = new ArrayList<>();

        // "{국가} {도시} 맛집" 검색 키워드 구성
        String country = reqDTO.getCountry();
        String city = reqDTO.getCity();
        String searchKeyword = country + " " + city + " 맛집";


        // Google Maps API 키 등록
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(googleApiKey)
                .build();

        //api 키 확인
        log.info("Google API Key: {}", googleApiKey);

        try {
            // Text Search API 호출
            PlacesSearchResponse response = PlacesApi.textSearchQuery(context, searchKeyword).await();
            if (response.results != null && response.results.length > 0) {
                // 검색한 모든 장소들에 대해, 장소 세부정보 API 호출
                for (PlacesSearchResult res : response.results) {
                    RecommPlaceRespDTO place = getPlaceDetails(res.placeId);
                    if (place != null) result.add(place);
                }
            } else
                log.info("No place found : {}", searchKeyword);
        } catch (Exception e) {
            throw new GoogleApiException(e);
        }
        return result;
    }

    // 장소 세부정보 API
    private RecommPlaceRespDTO getPlaceDetails(String placeId) {
        // Google Maps API 키 등록
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(googleApiKey)
                .disableRetries()  // 재시도 비활성화 (오류가 계속 발생할 경우 무한 루프 방지)
                .build();

        String photoUrl;

        try {
            // Details API 호출
            PlaceDetails details = PlacesApi.placeDetails(context, placeId).language("ko").await();

            // 응답 데이터 구조를 로그로 출력
            log.info("PlaceDetails Response: {}", details);

            // 예상치 못한 필드 무시
            if (details.secondaryOpeningHours != null) {
                log.warn("Ignoring secondaryOpeningHours field to avoid JSON parsing issues.");
            }

            // 이미지가 없는 장소는 결과에 포함하지 않음
            if (details.photos == null || details.photos.length == 0) {
                return null;
            }

            // 장소 이미지를 url로 저장 (maxwidth = 500 설정)
            photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=500&photoreference="
                    .concat(details.photos[0].photoReference) // 첫 번째 이미지 사용
                    .concat("&key=")
                    .concat(googleApiKey);

            // 영업 시간을 Map<String, String>으로 저장
            String[] weekdayText = details.currentOpeningHours.weekdayText;
            Map<String, String> openTime = convertToMap(weekdayText);

            return new RecommPlaceRespDTO(details, photoUrl, openTime);
        } catch (Exception e) {
            log.error("Error fetching place details for placeId {}: {}", placeId, e.getMessage());
            return null;
        }
    }

    // String[]으로 주어진 영업 시간을 요일별로 매핑되게끔 변환
    public static Map<String, String> convertToMap(String[] scheduleArray) {
        Map<String, String> scheduleMap = new LinkedHashMap<>();
        String currentDay = null;
        StringBuilder openTime = new StringBuilder();

        for (String entry : scheduleArray) {
            if (entry.contains(":")) {
                String[] parts = entry.split(": ", 2);
                if (parts.length == 2) {
                    if (currentDay != null) {
                        scheduleMap.put(currentDay, openTime.toString().trim());
                    }
                    currentDay = parts[0];
                    openTime = new StringBuilder(parts[1]);
                }
            } else {
                if (!openTime.isEmpty()) {
                    openTime.append(", ");
                }
                openTime.append(entry);
            }
        }

        // 마지막 요일 데이터 추가
        if (currentDay != null) {
            scheduleMap.put(currentDay, openTime.toString().trim());
        }

        return scheduleMap;
    }
}
