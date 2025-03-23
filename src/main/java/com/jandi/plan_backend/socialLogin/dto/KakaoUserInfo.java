package com.jandi.plan_backend.socialLogin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 카카오 사용자 정보 DTO
 * - id: 카카오에서 부여하는 고유 식별자 (long -> String 변환)
 * - email: 사용자가 동의한 경우에만 내려옴
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KakaoUserInfo {
    private String id;     // 카카오 userId (문자열로 변환)
    private String email;  // 카카오 계정 이메일 (동의 항목 꺼져있으면 null)
}
