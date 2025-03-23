package com.jandi.plan_backend.socialLogin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 구글에서 가져온 사용자 정보(간소화)
 * - sub : 구글의 사용자 식별자 (v2/userinfo에선 id, v3/userinfo에선 sub)
 * - email : 사용자의 이메일
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleUserInfo {
    private String sub;
    private String email;
}
