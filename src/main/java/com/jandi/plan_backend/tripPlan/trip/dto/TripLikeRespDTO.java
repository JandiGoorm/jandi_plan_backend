package com.jandi.plan_backend.tripPlan.trip.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 여행 계획 좋아요 추가 시 반환 DTO
 */
@Data
public class TripLikeRespDTO {
    private MyTripRespDTO trip;
    private LocalDateTime likedAt;

    public TripLikeRespDTO(MyTripRespDTO trip, LocalDateTime likedAt) {
        this.trip = trip;
        this.likedAt = likedAt;
    }
}
