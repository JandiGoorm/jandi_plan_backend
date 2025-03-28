package com.jandi.plan_backend.tripPlan.trip.dto;

import lombok.Getter;

/**
 * 여행 계획 작성자 정보를 담는 DTO
 */
@Getter
public class UserTripDTO {
    private final Integer userId;
    private final String userName;
    private final String profileImageUrl;

    public UserTripDTO(Integer userId, String userName, String profileImageUrl) {
        this.userId = userId;
        this.userName = userName;
        this.profileImageUrl = profileImageUrl;
    }
}
