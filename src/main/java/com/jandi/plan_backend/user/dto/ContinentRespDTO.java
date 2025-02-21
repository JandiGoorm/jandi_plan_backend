package com.jandi.plan_backend.user.dto;

import com.jandi.plan_backend.user.entity.Continent;
import lombok.Getter;

@Getter
public class ContinentRespDTO {
    private Integer continentId;
    private String name;
    private String imageUrl;
    private Integer searchCount;

    public ContinentRespDTO(Continent continent) {
        this.continentId = continent.getContinentId();
        this.name = continent.getName();
        this.imageUrl = continent.getImageUrl();
        this.searchCount = continent.getSearchCount();
    }
}
