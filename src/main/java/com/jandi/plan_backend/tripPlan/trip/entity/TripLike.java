package com.jandi.plan_backend.tripPlan.trip.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.jandi.plan_backend.user.entity.User;

import java.time.LocalDateTime;

/**
 * 여행 계획 좋아요 엔티티
 */
@Entity
@Table(name = "trip_like")
@IdClass(TripLikeId.class)
@Data
public class TripLike {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
