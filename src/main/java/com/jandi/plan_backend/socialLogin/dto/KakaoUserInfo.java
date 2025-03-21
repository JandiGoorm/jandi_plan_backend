package com.jandi.plan_backend.socialLogin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KakaoUserInfo {
    private String id;     // 카카오 회원번호
    private String email;  // 카카오계정 이메일 (선택)
}