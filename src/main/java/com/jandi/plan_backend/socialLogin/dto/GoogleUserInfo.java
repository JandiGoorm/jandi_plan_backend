package com.jandi.plan_backend.socialLogin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 구글 사용자 정보 (예: sub, email 등)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleUserInfo {
    private String sub;     // 구글 유저 고유 ID (OpenID Connect 시 'sub')
    private String email;   // 구글 계정 이메일
}
