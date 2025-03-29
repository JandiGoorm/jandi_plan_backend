package com.jandi.plan_backend.tripPlan.trip.dto;

import com.jandi.plan_backend.tripPlan.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.User;
import lombok.Getter;

/**
 * 내 여행 계획 전용 DTO (비공개 여부를 함께 반환)
 */
@Getter
public class MyTripRespDTO extends TripRespDTO {

    private final Boolean privatePlan;

    public MyTripRespDTO(User user,
                         String userProfileUrl,
                         Trip trip,
                         String cityImageUrl) {
        super(user, userProfileUrl, trip, cityImageUrl);
        this.privatePlan = trip.getPrivatePlan();
    }

    public MyTripRespDTO(User user,
                         String userProfileUrl,
                         Trip trip,
                         String cityImageUrl,
                         String tripImageUrl) {
        super(user, userProfileUrl, trip, cityImageUrl, tripImageUrl);
        this.privatePlan = trip.getPrivatePlan();
    }

    public MyTripRespDTO(TripRespDTO tripRespDTO, boolean privatePlan) {
        super(tripRespDTO);
        this.privatePlan = privatePlan;
    }
}
