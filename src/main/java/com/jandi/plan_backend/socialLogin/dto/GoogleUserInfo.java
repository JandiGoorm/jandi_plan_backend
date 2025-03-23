package com.jandi.plan_backend.socialLogin.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleUserInfo {
    private String sub;    // 구글 유저 고유 ID (v2/userinfo는 "id" 키)
    private String email;  // 구글 계정 이메일
}
