package com.jandi.plan_backend.user.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String userName;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}
