package com.jandi.plan_backend.trip.dto;

import lombok.Getter;

/**
 * 작성자 정보를 전달하기 위한 순수 DTO.
 * - userId, userName, 프로필 이미지 공개 URL을 보관합니다.
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
