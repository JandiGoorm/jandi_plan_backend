package com.jandi.plan_backend.commu.dto;

import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.user.entity.User;
import lombok.Getter;

/**
 * 게시물 조회 시 가져올 작성자 관련 DTO
 * 비밀번호를 포함한 유저의 모든 정보를 보내지 않도록 DTO로 조정
 */
@Getter
public class UserCommunityDTO {
    private final Integer userId;
    private final String userName;   // 사용자 아이디
    private final String firstName;  // 사용자 이름
    private final String lastName;   // 사용자 성
    private final String email;
    private final String profileImageUrl;

    public UserCommunityDTO(User user, ImageService imageService) {
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        // getPublicUrlByImageId를 호출하여 프로필 사진의 공개 URL을 가져옵니다.
        this.profileImageUrl = imageService.getPublicUrlByImageId(user.getUserId());
    }
}
