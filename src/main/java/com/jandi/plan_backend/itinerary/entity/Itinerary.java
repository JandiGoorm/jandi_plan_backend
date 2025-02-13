package com.jandi.plan_backend.itinerary.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.jandi.plan_backend.trip.entity.Trip;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Itinerary 클래스
 * - 'itinerary' 테이블과 매핑됨
 * - 여행 일정 정보를 날짜별로 저장하는 역할을 함
 * - 각 일정은 하나의 Trip에 속함
 */
@Entity
@Table(name = "itinerary")
@Data
public class Itinerary {

    // 기본키. 값은 데이터베이스에서 자동 증가됨.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer itineraryId;

    // 해당 일정이 속한 여행을 나타냄.
    // 여러 일정은 하나의 Trip에 속할 수 있음.
    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    // 일정의 날짜. null 값은 허용하지 않음.
    @Column(nullable = false)
    private LocalDate date;

    // 일정의 제목. 최대 255자까지 저장. null 값 허용.
    @Column(length = 255)
    private String title;

    // 일정에 대한 상세 설명.
    // TEXT 타입으로 저장되어 길이 제한이 없음.
    @Column(columnDefinition = "TEXT")
    private String description;

    // 일정이 생성된 시각.
    // null 값은 허용하지 않음.
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
