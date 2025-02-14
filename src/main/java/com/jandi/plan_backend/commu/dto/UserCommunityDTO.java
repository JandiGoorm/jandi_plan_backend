package com.jandi.plan_backend.commu.dto;

import com.jandi.plan_backend.user.entity.User;
import lombok.Getter;

@Getter
/**
 * 게시물 리스트 조회 시 가져올 게시자 관련 DTO
 * 비밀번호를 포함한 유저의 모든 정보를 보내지 않도록 DTO로 조정
 */
public class UserCommunityDTO {
    private Integer userId;
    private String userName;   // 사용자 아이디
    private String firstName;  // 사용자 이름
    private String lastName;   // 사용자 성
    private String email;

    public UserCommunityDTO(User user) {
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
    }
}
