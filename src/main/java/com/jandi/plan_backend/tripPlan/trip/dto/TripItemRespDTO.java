package com.jandi.plan_backend.tripPlan.trip.dto;

import lombok.Getter;

/**
 * 여행 계획 단일 조회 시, 비공개 여부와 liked 여부까지 포함
 */
@Getter
public class TripItemRespDTO extends TripRespDTO {

    private final Boolean privatePlan;
    private final Boolean liked;

    public TripItemRespDTO(TripRespDTO tripRespDTO, Boolean liked) {
        super(tripRespDTO);
        this.privatePlan = tripRespDTO.getPrivatePlan();
        this.liked = liked;
    }
}
