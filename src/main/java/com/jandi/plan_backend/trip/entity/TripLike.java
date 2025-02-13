package com.jandi.plan_backend.trip.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.jandi.plan_backend.user.entity.User;
import java.time.LocalDateTime;

/**
 * 엔티티 클래스.
 * 여행 계획 좋아요(Like) 정보를 저장하는 테이블(trip_like)과 매핑됨.
 *
 * 이 테이블은 특정 여행 계획(Trip)에 대해 사용자가 좋아요를 누른 기록을 저장한다.
 * 복합 기본키는 여행 계획과 사용자 정보를 조합하여 구성된다.
 *
 * 컬럼:
 * - trip_id: 여행 계획(Trip)에 대한 외래키, 복합 기본키의 일부.
 * - user_id: 사용자(User)에 대한 외래키, 복합 기본키의 일부.
 * - created_at: 좋아요가 생성된 시각을 기록하며 null 값을 허용하지 않음.
 */
@Entity
@Table(name = "trip_like")
@IdClass(TripLikeId.class)  // 복합 기본키를 정의한 클래스(TripLikeId)를 지정.
@Data
public class TripLike {

    /**
     * 여행 계획(Trip) 엔티티와의 다대일 관계.
     * 여러 개의 좋아요 기록은 하나의 여행 계획에 속한다.
     * 이 필드는 복합 기본키의 한 부분으로 사용됨.
     */
    @Id
    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    /**
     * 사용자(User) 엔티티와의 다대일 관계.
     * 여러 개의 좋아요 기록은 하나의 사용자에 속한다.
     * 이 필드 또한 복합 기본키의 한 부분으로 사용됨.
     */
    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 좋아요가 생성된 시각.
     * 좋아요가 기록된 정확한 시각을 저장하며, null 값을 허용하지 않음.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
