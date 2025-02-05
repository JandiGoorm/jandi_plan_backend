package com.jandi.plan_backend.entity.itinerary;

import jakarta.persistence.*;
import lombok.Data;
import com.jandi.plan_backend.entity.trip.Trip;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 여행 일정(날짜별) 테이블 (itinerary)
 *
 * itinerary_id (PK)
 * trip_id (FK)
 * date
 * title
 * description
 * created_at
 */
@Entity
@Table(name = "itinerary")
@Data
public class Itinerary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer itineraryId;

    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
