package com.jandi.plan_backend.itinerary.entity;

import com.jandi.plan_backend.trip.entity.Trip;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 일정 정보를 저장함.
 * 컬럼:
 * - itinerary_id: 기본키, 자동 증가
 * - trip_id: 여행과의 다대일 관계 (FK)
 * - place_id: 장소와의 다대일 관계 (FK)
 * - date: 일정 날짜, null 불가
 * - start_time: 일정 시작 시간, null 불가
 * - end_time: 일정 종료 시간, null 불가
 * - title: 일정 제목, 최대 255자, null 불가
 * - cost: 일정 비용, null 불가
 * - created_at: 일정 생성 시각, null 불가
 */
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

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false)
    private Integer cost;

    @Column(nullable = false)
    private LocalDate createdAt;
}