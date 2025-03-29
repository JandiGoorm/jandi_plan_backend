package com.jandi.plan_backend.commu.community.dto;

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

    // 프로필 이미지 같이 넘겨주는 버전
    public UserCommunityDTO(User user, ImageService imageService) {
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        // 프로필 이미지 URL을 가져옴 (예: imageService에서 최신 URL 반환)
        this.profileImageUrl = imageService.getImageByTarget("profile", user.getUserId())
                .map(img -> "https://storage.googleapis.com/plan-storage/" + img.getImageUrl())
                .orElse(null);
    }

    // 프로필 이미지 없이 넘겨주는 버전 (단일 인자 생성자)
    public UserCommunityDTO(User user) {
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.profileImageUrl = null; // 또는 기본 이미지 URL을 지정할 수 있음
    }
}
