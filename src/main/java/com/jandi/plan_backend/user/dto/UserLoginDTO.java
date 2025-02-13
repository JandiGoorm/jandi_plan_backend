package com.jandi.plan_backend.user.dto;

import lombok.Data;

/**
 * UserLoginDTO 클래스
 * 로그인 요청 시 클라이언트로부터 전달되는 데이터를 담는 DTO.
 * 이메일과 비밀번호를 저장한다.
 */
@Data
public class UserLoginDTO {

    // 사용자가 로그인할 때 입력한 이메일 주소
    private String email;

    // 사용자가 로그인할 때 입력한 비밀번호
    private String password;
}
