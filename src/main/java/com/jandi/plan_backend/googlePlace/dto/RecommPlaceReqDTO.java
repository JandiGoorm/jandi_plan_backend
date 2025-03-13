package com.jandi.plan_backend.googlePlace.dto;

import lombok.Data;

/**
 * 맛집 검색 시, 국가(country)와 도시(city)를 입력받는 DTO
 */
@Data
public class RecommPlaceReqDTO {
    private String country;
    private String city;
}
