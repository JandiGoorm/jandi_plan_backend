package com.jandi.plan_backend.tripPlan.trip.dto;

import lombok.Data;

/**
 * 여행 계획 수정 요청 DTO
 */
@Data
public class TripUpdateReqDTO {
    private String title;
    /**
     * "yes" (비공개) or "no" (공개)
     */
    private String privatePlan;
}
