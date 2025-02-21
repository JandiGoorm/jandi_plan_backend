package com.jandi.plan_backend.user.dto;

import com.jandi.plan_backend.user.entity.Country;
import lombok.Getter;

@Getter
public class CountryRespDTO {
    private Integer countryId;
    private String name;
    private Integer searchCount;
    private ContinentRespDTO continent;

    public CountryRespDTO(Country country) {
        this.countryId = country.getCountryId();
        this.name = country.getName();
        this.searchCount = country.getSearchCount();
        this.continent = new ContinentRespDTO(country.getContinent());
    }
}
