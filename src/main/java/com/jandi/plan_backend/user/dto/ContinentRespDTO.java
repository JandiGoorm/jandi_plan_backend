package com.jandi.plan_backend.user.dto;

import com.jandi.plan_backend.user.entity.Continent;
import lombok.Getter;

@Getter
public class ContinentRespDTO {
    private Integer continentId;
    private String name;
    private String imageUrl;
    private Integer searchCount;

    // 기존 생성자: Continent + imageUrl
    public ContinentRespDTO(Continent continent, String imageUrl) {
        this.continentId = continent.getContinentId();
        this.name = continent.getName();
        this.imageUrl = imageUrl; // 추가 파라미터
        this.searchCount = continent.getSearchCount();
    }

    // 새로 추가: Continent만 받아서 imageUrl은 null 세팅
    public ContinentRespDTO(Continent continent) {
        this(continent, null);  // 내부적으로 위 생성자를 호출
    }
}