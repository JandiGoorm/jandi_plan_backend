package com.jandi.plan_backend.entity.user;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 사용자 정보 테이블 (users)
 *
 * user_id (PK, auto-increment)
 * user_name
 * first_name
 * last_name
 * email (unique)
 * password
 * created_at
 * updated_at
 * verified (이메일 인증 여부)
 */
@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(nullable = false, length = 50)
    private String userName;   // 사용자 아이디

    @Column(nullable = false, length = 50)
    private String firstName;  // 사용자 이름

    @Column(nullable = false, length = 50)
    private String lastName;   // 사용자 성

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean verified;
}
