package com.jandi.plan_backend.trip.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TripLikeRespDTO {
    private MyTripRespDTO trip;
    private LocalDateTime likedAt;

    public TripLikeRespDTO(MyTripRespDTO trip, LocalDateTime likedAt) {
        this.trip = trip;
        this.likedAt = likedAt;
    }
}
