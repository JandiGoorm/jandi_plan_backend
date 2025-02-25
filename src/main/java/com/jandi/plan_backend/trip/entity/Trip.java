package com.jandi.plan_backend.trip.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.jandi.plan_backend.user.entity.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 엔티티 클래스. "trip" 테이블에 매핑됨.
 * 여행 계획 정보를 저장함.
 */
@Entity
@Table(name = "trip")
@Data
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tripId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean privatePlan;

    @Column(nullable = false)
    private Integer likeCount;

    /**
     * TripLike 엔티티와의 1:N 관계.
     * Trip 삭제 시 TripLike도 자동으로 삭제되도록 cascade와 orphanRemoval 설정.
     */
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripLike> tripLikes;

    /**
     * TripParticipant 엔티티와의 1:N 관계.
     * Trip 삭제 시 TripParticipant도 자동으로 삭제되도록 cascade와 orphanRemoval 설정.
     */
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripParticipant> participants;
}
