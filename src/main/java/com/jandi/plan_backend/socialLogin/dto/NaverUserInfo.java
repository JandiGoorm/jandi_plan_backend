package com.jandi.plan_backend.socialLogin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 네이버 사용자 정보 DTO
 * - id: 네이버에서 부여하는 고유 식별자 (문자열)
 * - email: 사용자가 동의한 경우에만 내려옴
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NaverUserInfo {
    private String id;
    private String email;
}
