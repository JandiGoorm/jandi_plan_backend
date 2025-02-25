package com.jandi.plan_backend.itinerary.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 장소 정보를 저장함.
 * 컬럼:
 * - place_id: 기본키, 자동 증가
 * - name: 장소 이름, 최대 255자, null 불가
 * - address: 도로명 주소, 최대 255자, null 불가
 * - latitude: 위도, null 불가
 * - longitude: 경도, null 불가
 * - saved_count: 저장된 횟수, 기본값 0
 * - viewCount: 조회수 (검색 횟수 포함), 기본값 0
 */
@Entity
@Table(name = "place")
@Data
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long placeId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;
}