package com.jandi.plan_backend.trip.dto;

import lombok.Data;

/**
 * 여행 계획 수정 요청 DTO
 */
@Data
public class TripUpdateReqDTO {
    private String title;
    // "yes" 또는 "no" 문자열로 전달 (yes = 비공개, no = 공개)
    private String privatePlan;
}
