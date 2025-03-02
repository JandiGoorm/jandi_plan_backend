package com.jandi.plan_backend.commu.entity;

import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.trip.entity.TripLikeId;
import com.jandi.plan_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "community_like")
@IdClass(CommunityLikeId.class)
@Data
public class CommunityLike {
    @Id
    @ManyToOne
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
