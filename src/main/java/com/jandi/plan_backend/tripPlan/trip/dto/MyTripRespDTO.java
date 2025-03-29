package com.jandi.plan_backend.tripPlan.trip.dto;

import lombok.Getter;

/**
 * 내 여행 계획 전용 DTO (비공개 여부를 함께 반환)
 */
@Getter
public class MyTripRespDTO extends TripRespDTO {

    private final Boolean privatePlan;

    public MyTripRespDTO(TripRespDTO tripRespDTO, boolean privatePlan) {
        super(tripRespDTO);
        this.privatePlan = privatePlan;
    }
}
