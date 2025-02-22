package com.jandi.plan_backend.trip.dto;

import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.user.entity.User;
import lombok.Getter;

@Getter
public class UserTripDTO {
    private final Integer userId;
    private final String userName;   // 사용자 아이디
    private final String profileImageUrl;

    public UserTripDTO(User user, ImageService imageService) {
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        // getPublicUrlByImageId를 호출하여 프로필 사진의 공개 URL을 가져옵니다.
        this.profileImageUrl = imageService.getPublicUrlByImageId(user.getUserId());
    }
}
