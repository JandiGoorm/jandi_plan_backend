package com.jandi.plan_backend.user.dto;

import lombok.Data;

/**
 * 닉네임 변경 요청 시 사용할 DTO
 */
@Data
public class UserNameUpdateDTO {
    private String newUserName;
}
