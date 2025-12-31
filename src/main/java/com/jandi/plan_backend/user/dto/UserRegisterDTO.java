package com.jandi.plan_backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDTO {
    @NotBlank(message = "사용자명은 필수입니다")
    @Size(min = 2, max = 50, message = "사용자명은 2~50자여야 합니다")
    @Pattern(regexp = "^[a-zA-Z0-9_가-힣]+$", message = "사용자명은 영문, 숫자, 밑줄, 한글만 허용됩니다")
    private String userName;

    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 1, max = 50, message = "이름은 1~50자여야 합니다")
    private String firstName;

    @NotBlank(message = "성은 필수입니다")
    @Size(min = 1, max = 50, message = "성은 1~50자여야 합니다")
    private String lastName;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이어야 합니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 100, message = "비밀번호는 8~100자여야 합니다")
    private String password;
}
