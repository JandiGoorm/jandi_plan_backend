package com.jandi.plan_backend.trip.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.jandi.plan_backend.user.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 여행 계획 테이블 (trip)
 *
 * trip_id (PK)
 * user_id (FK)
 * title
 * description
 * start_date
 * end_date
 * created_at
 * updated_at
 * private (boolean)
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

    @Column(nullable = false, length = 255)
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
}
