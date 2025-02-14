package com.jandi.plan_backend.user.dto;

import lombok.Data;

/**
 * UserRegisterDTO 클래스
 * 회원 가입 요청 시 클라이언트로부터 전달받은 데이터를 담는 Data Transfer Object.
 * 사용자 등록에 필요한 정보들을 포함한다.
 */
@Data
public class UserRegisterDTO {

    // 회원가입에 사용할 고유 사용자 아이디
    private String userName;

    // 사용자의 이름 (예: John)
    private String firstName;

    // 사용자의 성 (예: Doe)
    private String lastName;

    // 사용자의 이메일 주소
    private String email;

    // 사용자가 설정한 비밀번호
    private String password;
}
