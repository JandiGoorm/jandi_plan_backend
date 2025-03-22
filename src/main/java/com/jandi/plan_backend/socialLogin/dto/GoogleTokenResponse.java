package com.jandi.plan_backend.socialLogin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 구글 토큰 응답 (예: access_token, refresh_token, expires_in 등)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleTokenResponse {
    private String accessToken;
    private String refreshToken;
    private int expiresIn;
}
