package com.jandi.plan_backend.user.dto;

import com.jandi.plan_backend.user.entity.City;
import lombok.Getter;

@Getter
public class CityRespDTO {
    private Integer cityId;
    private String name;
    private String description;
    private String imageUrl;
    private Integer searchCount;
    private Integer likeCount;
    private CountryRespDTO country;

    public CityRespDTO(City destination, String imageUrl) {
        this.cityId = destination.getCityId();
        this.name = destination.getName();
        this.description = destination.getDescription();
        this.imageUrl = imageUrl;
        this.searchCount = destination.getSearchCount();
        this.likeCount = destination.getLikeCount();
        this.country = new CountryRespDTO(destination.getCountry());
    }
}
