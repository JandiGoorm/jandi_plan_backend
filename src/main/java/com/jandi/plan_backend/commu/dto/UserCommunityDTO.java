package com.jandi.plan_backend.commu.dto;

import com.jandi.plan_backend.storage.service.ImageService;
import com.jandi.plan_backend.user.dto.UserRegisterDTO;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import lombok.Getter;

@Getter
/**
 * 게시물 조회 시 가져올 작성자 관련 DTO
 * 비밀번호를 포함한 유저의 모든 정보를 보내지 않도록 DTO로 조정
 */
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
        this.profileImageUrl = imageService.getUserProfile(user.getUserId());
    }
}
