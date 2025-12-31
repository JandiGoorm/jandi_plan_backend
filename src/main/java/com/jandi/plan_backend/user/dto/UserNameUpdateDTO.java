package com.jandi.plan_backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 닉네임 변경 요청 시 사용할 DTO
 */
@Data
public class UserNameUpdateDTO {
    @NotBlank(message = "새 사용자명은 필수입니다")
    @Size(min = 2, max = 50, message = "사용자명은 2~50자여야 합니다")
    @Pattern(regexp = "^[a-zA-Z0-9_가-힣]+$", message = "사용자명은 영문, 숫자, 밑줄, 한글만 허용됩니다")
    private String newUserName;
}
