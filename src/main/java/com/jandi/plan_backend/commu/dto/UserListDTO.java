package com.jandi.plan_backend.commu.dto;

import com.jandi.plan_backend.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserListDTO {
    private Integer userId;
    private String userName;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime createdAt;
    private Boolean reported;
    private Boolean verified;

    public UserListDTO(User user) {
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.createdAt = user.getCreatedAt();
        this.reported = user.getReported();
        this.verified = user.getVerified();
    }
}
