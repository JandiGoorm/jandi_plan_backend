package com.jandi.plan_backend.user.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 사용자 상세 정보를 반환하기 위한 DTO.
 */
@Data
public class UserInfoResponseDto {
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String username;
    private boolean verified;
    private boolean reported;
    private String profileImageUrl;
}
