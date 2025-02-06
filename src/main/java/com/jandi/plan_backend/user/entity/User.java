package com.jandi.plan_backend.user.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 사용자 정보 테이블 (users)
 */
@Entity
@Table(name = "users")  // user -> 예약어 충돌 우려, "users"로 해둠
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
    private Boolean verified; // 이메일 인증 여부

    /**
     * 이메일 인증 코드
     * 회원가입 시 생성하여 사용자에게 이메일로 전송하고, 인증 시 확인
     */
    @Column(length = 20)
    private String emailVerificationCode;
}
