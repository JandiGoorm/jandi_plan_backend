package com.jandi.plan_backend.trip.dto;

import lombok.Getter;
import java.time.LocalDate;

/**
 * 내 여행 계획 정보를 전달하기 위한 DTO.
 * TripRespDTO에 여행 계획의 공개 여부(privatePlan)를 추가합니다.
 */
@Getter
public class MyTripRespDTO extends TripRespDTO {
    private final Boolean privatePlan;

    public MyTripRespDTO(UserTripDTO user, Integer tripId, String title, LocalDate startDate, LocalDate endDate,
                         String description, Integer likeCount, String imageUrl, Boolean privatePlan) {
        super(user, tripId, title, startDate, endDate, description, likeCount, imageUrl);
        this.privatePlan = privatePlan;
    }
}
