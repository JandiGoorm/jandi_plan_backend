package com.jandi.plan_backend.trip.dto;

import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.User;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class MyTripRespDTO extends TripRespDTO {
    private final Boolean privatePlan;

    public MyTripRespDTO(UserTripDTO user, Integer tripId, String title, LocalDate startDate, LocalDate endDate,
                         String description, Integer likeCount, String imageUrl, Boolean privatePlan) {
        super(user, tripId, title, startDate, endDate, description, likeCount, imageUrl);
        this.privatePlan = privatePlan;
    }

    public MyTripRespDTO(User user, String userProfileUrl, Trip trip, String TripImageUrl){
        super(user, userProfileUrl, trip, TripImageUrl);
        this.privatePlan = trip.getPrivatePlan();
    }
}
