package com.jandi.plan_backend.itinerary.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Transportation 엔티티
 *
 * 이 클래스는 데이터베이스의 'transportation' 테이블과 매핑되며,
 * 교통편 관련 정보를 저장하는 역할을 함.
 *
 * 컬럼 설명:
 * - transportation_id: 교통편 정보를 식별하는 기본키(PK). 데이터베이스에서 자동으로 증가됨.
 * - itinerary_id: 해당 교통편이 속한 일정(Itinerary)의 식별자(FK).
 *                여러 교통편이 하나의 일정에 연결될 수 있음.
 * - created_at: 교통편 정보가 생성된 시각. null 값 허용 안 함.
 * - category: 교통편의 종류를 나타내는 문자열 (예: "버스", "기차", "비행기" 등). 최대 50자.
 * - start_place: 출발지 정보를 나타냄. 최대 255자.
 * - end_place: 도착지 정보를 나타냄. 최대 255자.
 * - duration: 이동에 걸린 시간. 분이나 시간 단위로 저장할 수 있음.
 */
@Entity
@Table(name = "transportation")
@Data
public class Transportation {

    // transportation_id 컬럼. PK이며, DB가 자동으로 값을 생성.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transportationId;

    // 일정(Itinerary)와 다대일 관계 설정.
    // 여러 교통편 정보가 하나의 일정에 속할 수 있다.
    @ManyToOne
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    // 교통편 정보가 생성된 시각을 저장. null 값은 허용하지 않음.
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 교통편 종류 (예: "버스", "기차", "비행기" 등). 길이 제한 50.
    @Column(length = 50)
    private String category;

    // 출발지 정보. 최대 길이 255자로 제한.
    @Column(length = 255)
    private String startPlace;

    // 도착지 정보. 최대 길이 255자로 제한.
    @Column(length = 255)
    private String endPlace;

    // 이동 소요 시간. 분 또는 시간 단위로 저장 가능.
    private Integer duration;
}
