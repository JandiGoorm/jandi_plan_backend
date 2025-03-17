package com.jandi.plan_backend.user.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserInfoRespDTO {
    private Integer userId;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String username;
    private boolean verified;
    private boolean reported;
    private String profileImageUrl;
    private String role;
}
