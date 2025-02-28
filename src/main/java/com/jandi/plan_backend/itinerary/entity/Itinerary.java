package com.jandi.plan_backend.itinerary.entity;

import com.jandi.plan_backend.trip.entity.Trip;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "itinerary")
@Data
public class Itinerary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itineraryId;

    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false)
    private Long placeId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false)
    private Integer cost;

    @Column(nullable = false)
    private LocalDate createdAt;
}
