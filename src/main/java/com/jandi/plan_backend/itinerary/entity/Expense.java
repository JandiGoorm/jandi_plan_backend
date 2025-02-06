package com.jandi.plan_backend.itinerary.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.jandi.plan_backend.trip.entity.Trip;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 여행 경비(비용) 정보 테이블 (expense)
 *
 * expense_id (PK)
 * trip_id (FK)
 * itinerary_id (FK, 일정별 비용인 경우)
 * category
 * amount
 * description
 * created_at
 * updated_at
 * transportation_id (FK)
 * accommodation_id (FK)
 */
@Entity
@Table(name = "expense")
@Data
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer expenseId;

    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    // 일정별 비용이면 값이 들어가고, 여행 전체 비용이면 null
    @ManyToOne
    @JoinColumn(name = "itinerary_id")
    private Itinerary itinerary;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "transportation_id")
    private Transportation transportation;

    @ManyToOne
    @JoinColumn(name = "accommodation_id")
    private Accommodation accommodation;
}
