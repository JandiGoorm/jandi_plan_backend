package com.jandi.plan_backend.googlePlace.dto;

import com.google.maps.model.PlaceDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommPlaceRespDTO {
    private String placeId; // 장소의 고유 ID
    private String name; // 식당 이름
    private String url; // Google Maps 상세 URL
    private double rating; // 평점
    private String photoUrl; // 이미지 URL
    private String address; // 주소
    private double latitude; // 위도
    private double longitude; // 경도
    private int ratingCount; // 리뷰수
    private Map<String, String> openTime; // 영업 시간(요일별)
    private boolean dineIn; //매장 식사 가능 여부

    public RecommPlaceRespDTO(PlaceDetails details, String photoUrl, Map<String, String> openTime) {
        this.name = details.name;
        this.placeId = details.placeId;
        this.address = details.formattedAddress;
        this.openTime = openTime;
        this.dineIn = details.dineIn;
        this.latitude = details.geometry.location.lat;
        this.longitude = details.geometry.location.lng;
        this.rating = (double) Math.round(details.rating * 100) / 100;
        this.ratingCount = details.userRatingsTotal;
        this.url = details.url.toString();
        this.photoUrl = photoUrl;
    }
}
