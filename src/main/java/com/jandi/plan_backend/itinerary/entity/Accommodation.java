package com.jandi.plan_backend.itinerary.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.jandi.plan_backend.trip.entity.Trip;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 숙박 정보 테이블 (accommodation)
 *
 * accommodation_id (PK)
 * trip_id (FK)
 * created_at
 * address
 * check_in_date
 * check_out_date
 * description
 */
@Entity
@Table(name = "accommodation")
@Data
public class Accommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer accommodationId;

    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 255)
    private String address;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    @Column(columnDefinition = "TEXT")
    private String description;
}
