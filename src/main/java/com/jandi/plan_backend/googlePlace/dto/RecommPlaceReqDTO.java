package com.jandi.plan_backend.googlePlace.dto;

import lombok.Data;

@Data
public class RecommPlaceReqDTO {
    // cityId를 입력받아 해당 도시 정보를 DB에서 조회
    private Integer cityId;
}
