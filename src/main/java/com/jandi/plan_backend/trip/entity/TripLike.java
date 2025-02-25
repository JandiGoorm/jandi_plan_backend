package com.jandi.plan_backend.trip.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.jandi.plan_backend.user.entity.User;
import java.time.LocalDateTime;

/**
 * 여행 계획 좋아요(Like) 정보를 저장하는 테이블(trip_like)과 매핑됨.
 */
@Entity
@Table(name = "trip_like")
@IdClass(TripLikeId.class)
@Data
public class TripLike {

    @Id
    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
