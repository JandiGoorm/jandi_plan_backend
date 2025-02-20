package com.jandi.plan_backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 사용자 인증 응답 DTO.
 * 액세스 토큰과 리프레시 토큰을 포함한다.
 */
@Data
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
}
