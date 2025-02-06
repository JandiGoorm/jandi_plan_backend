package com.jandi.plan_backend.user.dto;

import lombok.Data;

/**
 * 로그인 시 주고받는 DTO
 */
@Data
public class UserLoginDTO {
    private String email;
    private String password;
}