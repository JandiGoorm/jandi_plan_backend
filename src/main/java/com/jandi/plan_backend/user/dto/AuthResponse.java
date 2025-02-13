package com.jandi.plan_backend.user.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * AuthResponse DTO
 *
 * 이 클래스는 인증 성공 후 클라이언트에 반환할 응답 객체임.
 * JWT 액세스 토큰을 저장하며, 클라이언트는 이 토큰을 이용해
 * 이후 요청 시 인증을 진행할 수 있음.
 */
@Data
@RequiredArgsConstructor
public class AuthResponse {

    /**
     * JWT 액세스 토큰.
     * 최종적으로 사용자에게 전달되어 인증 헤더에 포함됨.
     */
    private final String accessToken;
}
