package com.jandi.plan_backend.user.dto;

import com.jandi.plan_backend.user.entity.City;
import lombok.Getter;

@Getter
public class CityRespDTO {
    private Integer destinationId;
    private String name;
    private String description;
    private String imageUrl;
    private Integer searchCount;
    private CountryRespDTO country;

    public CityRespDTO(City destination) {
        this.destinationId = destination.getDestinationId();
        this.name = destination.getName();
        this.description = destination.getDescription();
        this.imageUrl = destination.getImageUrl();
        this.searchCount = destination.getSearchCount();
        this.country = new CountryRespDTO(destination.getCountry());
    }
}
