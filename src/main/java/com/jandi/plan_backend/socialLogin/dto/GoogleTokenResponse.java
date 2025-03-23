package com.jandi.plan_backend.socialLogin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleTokenResponse {
    private String accessToken;   // "access_token"
    private String refreshToken;  // "refresh_token"
    private int expiresIn;        // "expires_in"
}
