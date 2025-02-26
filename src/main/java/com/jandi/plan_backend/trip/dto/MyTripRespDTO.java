package com.jandi.plan_backend.trip.dto;

import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.User;
import lombok.Getter;

@Getter
public class MyTripRespDTO extends TripRespDTO {
    private final Boolean privatePlan;

    public MyTripRespDTO(User user, String userProfileUrl, Trip trip){
        super(user, userProfileUrl, trip);
        this.privatePlan = trip.getPrivatePlan();
    }
}
