package com.jandi.plan_backend.trip.dto;

import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.User;
import lombok.Getter;

/**
 * 내 여행 계획 정보를 전달하기 위한 DTO.
 * TripRespDTO에 여행 계획의 공개 여부(privatePlan)를 추가합니다.
 */
@Getter
public class MyTripRespDTO extends TripRespDTO {
    private final Boolean privatePlan;

    public MyTripRespDTO(User user, String userProfileUrl, Trip trip, String TripImageUrl){
        super(user, userProfileUrl, trip, TripImageUrl);
        this.privatePlan = trip.getPrivatePlan();
    }
}
