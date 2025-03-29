package com.jandi.plan_backend.tripPlan.trip.dto;

import lombok.Data;

/**
 * 여행 계획 생성 요청 DTO
 */
@Data
public class TripCreateReqDTO {
    private String title;
    private String startDate;
    private String endDate;
    /**
     * "yes" (비공개) or "no" (공개)
     */
    private String privatePlan;
    private Integer budget;
    private Integer cityId;
}
