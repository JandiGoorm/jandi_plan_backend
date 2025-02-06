package com.jandi.plan_backend.user.dto;

import lombok.Data;

/**
 * 회원가입 시 주고 받는 DTO
 */
@Data
public class UserRegisterDTO {
    private String userName;     // 아이디 (또는 별도 닉네임)
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}