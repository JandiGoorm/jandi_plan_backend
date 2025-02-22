package com.jandi.plan_backend.trip.dto;

import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.trip.entity.Trip;
import lombok.Getter;

@Getter
public class MyTripRespDTO extends TripRespDTO{
    private Boolean privatePlan;

    public MyTripRespDTO(Trip trip, ImageService imageService) {
        super(trip, imageService);
        this.privatePlan = trip.getPrivatePlan();
    }
}
