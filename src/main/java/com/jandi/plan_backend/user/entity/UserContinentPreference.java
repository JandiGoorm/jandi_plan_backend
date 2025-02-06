package com.jandi.plan_backend.user.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 사용자 선호 대륙 정보 테이블 (user_continent_preference)
 *
 * user_id (FK)
 * continent_id (FK)
 * created_at
 */
@Entity
@Table(name = "user_continent_preference")
@IdClass(UserContinentPreferenceId.class)
@Data
public class UserContinentPreference {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "continent_id", nullable = false)
    private Continent continent;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
