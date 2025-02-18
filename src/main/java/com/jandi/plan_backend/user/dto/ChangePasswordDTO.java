package com.jandi.plan_backend.user.dto;

import lombok.Data;

/**
 * 비밀번호 변경 요청을 위한 DTO.
 * - currentPassword: 현재 비밀번호
 * - newPassword: 새 비밀번호
 */
@Data
public class ChangePasswordDTO {
    private String currentPassword;
    private String newPassword;
}
