package com.jandi.plan_backend.socialLogin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 카카오 액세스 토큰 응답 DTO
 * - accessToken
 * - refreshToken
 * - expiresIn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KakaoTokenResponse {
    private String accessToken;
    private String refreshToken;
    private int expiresIn;
}
