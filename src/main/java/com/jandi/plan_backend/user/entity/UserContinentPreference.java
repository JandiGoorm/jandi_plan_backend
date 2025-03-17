package com.jandi.plan_backend.user.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_continent_preference")
@IdClass(UserContinentPreferenceId.class) // 복합 기본키 클래스로 UserContinentPreferenceId를 사용
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
