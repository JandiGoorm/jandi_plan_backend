package com.jandi.plan_backend.user.dto;

import lombok.Data;

@Data
public class UserLoginDTO {
    private String email;
    private String password;
}
