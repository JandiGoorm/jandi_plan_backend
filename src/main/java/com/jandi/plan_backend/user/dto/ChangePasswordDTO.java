package com.jandi.plan_backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 비밀번호 변경 요청을 위한 DTO.
 * - currentPassword: 현재 비밀번호
 * - newPassword: 새 비밀번호
 */
@Data
public class ChangePasswordDTO {
    @NotBlank(message = "현재 비밀번호는 필수입니다")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호는 필수입니다")
    @Size(min = 8, max = 100, message = "비밀번호는 8~100자여야 합니다")
    private String newPassword;
}
