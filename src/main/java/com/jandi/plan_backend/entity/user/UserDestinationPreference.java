package com.jandi.plan_backend.entity.user;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 사용자 선호 주요 여행지 정보 테이블 (user_destination_preference)
 *
 * user_id (FK)
 * destination_id (FK)
 * created_at
 */
@Entity
@Table(name = "user_destination_preference")
@IdClass(UserDestinationPreferenceId.class)
@Data
public class UserDestinationPreference {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "destination_id", nullable = false)
    private MajorDestination majorDestination;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
