package com.jandi.plan_backend.entity.itinerary;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 교통편 정보 테이블 (transportation)
 *
 * transportation_id (PK)
 * itinerary_id (FK)
 * created_at
 * category
 * start_place
 * end_place
 * duration
 */
@Entity
@Table(name = "transportation")
@Data
public class Transportation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transportationId;

    @ManyToOne
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 50)
    private String category;

    @Column(length = 255)
    private String startPlace;

    @Column(length = 255)
    private String endPlace;

    // 소요 시간 (분 또는 시간)
    private Integer duration;
}
