package com.jandi.plan_backend.entity.itinerary;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 일정 내 목적지 정보 테이블 (destination)
 *
 * destination_id (PK)
 * itinerary_id (FK)
 * name
 * address
 * latitude
 * longitude
 * arrival_time
 * departure_time
 * created_at
 */
@Entity
@Table(name = "destination")
@Data
public class Destination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer destinationId;

    @ManyToOne
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 255)
    private String address;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    private LocalTime arrivalTime;

    private LocalTime departureTime;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
