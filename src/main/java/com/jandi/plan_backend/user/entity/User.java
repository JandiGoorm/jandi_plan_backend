package com.jandi.plan_backend.user.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 사용자 정보 테이블 (users)
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
    private Boolean verified; // 이메일 인증 여부

    /**
     * 이메일 인증 토큰(링크 클릭 시 검증)
     */
    @Column
    private String verificationToken;

    /**
     * 인증 토큰 만료 시간(선택)
     */
    @Column
    private LocalDateTime tokenExpires;
}
