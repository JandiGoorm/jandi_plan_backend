package com.jandi.plan_backend.trip.dto;

import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.User;
import lombok.Getter;

/**
 * 본인 여행 계획 전용 DTO
 * TripRespDTO를 상속하여 privatePlan 등 추가 정보를 포함
 */
@Getter
public class MyTripRespDTO extends TripRespDTO {
    private final Boolean privatePlan;

    // 4개 인자 생성자 (tripImageUrl = null)
    public MyTripRespDTO(User user,
                         String userProfileUrl,
                         Trip trip,
                         String cityImageUrl) {
        super(user, userProfileUrl, trip, cityImageUrl);
        this.privatePlan = trip.getPrivatePlan();
    }

    // 5개 인자 생성자 (tripImageUrl 포함)
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
