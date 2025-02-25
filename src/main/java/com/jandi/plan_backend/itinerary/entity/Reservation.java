package com.jandi.plan_backend.itinerary.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 예약 정보를 저장함.
 * 컬럼:
 * - reservation_id: 기본키, 자동 증가
 * - trip_id: 여행과의 다대일 관계 (FK)
 * - category: 예약 종류 (ENUM)
 * - title: 예약 제목, 최대 255자, null 불가
 * - description: 예약 상세 정보, 최대 255자, null 불가
 * - cost: 예약 비용, null 불가
 * - payment_status: 결제 여부 (보류)
 */
@Entity
@Table(name = "reservation")
@Data
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @Column(nullable = false)
    private Long tripId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationCategory category;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false)
    private Integer cost;
}