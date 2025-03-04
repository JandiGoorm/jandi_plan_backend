package com.jandi.plan_backend.commu.dto;

import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.user.entity.User;
import lombok.Getter;

@Getter
public class UserCommunityDTO {
    private final Integer userId;
    private final String userName;
    private final String firstName;
    private final String lastName;
    private final String email;
    private String profileImageUrl;

    //프로필 이미지 같이 넘겨주는 버전
    public UserCommunityDTO(User user, ImageService imageService) {
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        // getPublicUrlByImageId를 호출하여 프로필 사진의 공개 URL을 가져옵니다.
        this.profileImageUrl = imageService.getPublicUrlByImageId(user.getUserId());
    }

    //프로필 이미지 없이 넘겨주는 버전
    public UserCommunityDTO(User user) {
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
    }
}
