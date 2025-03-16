package com.jandi.plan_backend.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * role 변경을 추적 기록하는 엔티티
 */

@Entity
@Table(name = "role_log")
@Data
public class RoleLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false) // 변경된 사용자의 ID
    private User user;

    @Column(nullable = false)
    private int prevRole; // 변경 전 역할

    @Column(nullable = false)
    private int newRole; // 변경 후 역할

    @Column(nullable = false)
    private LocalDateTime changedAt; // 변경 시각

    @Column(nullable = false)
    private String changedBy; // 변경 요청자의 이메일 주소 or 시스템
}
