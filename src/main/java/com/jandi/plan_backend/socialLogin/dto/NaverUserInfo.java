package com.jandi.plan_backend.socialLogin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NaverUserInfo {
    private String id;
    private String email;
}