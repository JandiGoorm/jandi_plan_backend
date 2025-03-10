package com.jandi.plan_backend.openai.dto;

import lombok.Data;

@Data
public class AiRestaurantDTO {
    private Long id;              // DB 식별자 (노출 필요 없다면 생략 가능)
    private Integer cityId;       // 도시 ID
    private String name;          // 식당 이름
    private double latitude;      // 위도
    private double longitude;     // 경도
    private double rating;        // 평점(5점 만점)
    private String description;   // 상세 설명
    private String imageUrl;      // 이미지 URL
}
