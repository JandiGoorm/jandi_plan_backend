package com.jandi.plan_backend.itinerary.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.jandi.plan_backend.trip.entity.Trip;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 숙박 정보 엔티티 클래스.
 * "accommodation" 테이블과 매핑됨.
 *
 * 필드 설명:
 * - accommodationId: 숙박 정보의 고유 식별자. PK이며, DB가 자동으로 값 생성.
 * - trip: 숙박 정보가 속한 여행 계획(Trip) 엔티티와 다대일 관계를 맺음.
 * - createdAt: 숙박 정보 생성 시각. null 불가.
 * - address: 숙박지 주소. 최대 255자 제한.
 * - checkInDate: 체크인 날짜.
 * - checkOutDate: 체크아웃 날짜.
 * - description: 숙박에 대한 상세 설명. TEXT 타입으로 저장.
 */
@Entity
@Table(name = "accommodation")
@Data
public class Accommodation {

    // 숙박 정보의 기본키. DB가 자동 증가 전략으로 값을 할당.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer accommodationId;

    // 숙박 정보와 연결된 여행 계획.
    // 여러 숙박 정보가 하나의 여행 계획에 속할 수 있음.
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    // 숙박 정보가 생성된 시각.
    // 생성 시각은 null이 될 수 없음.
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 숙박지 주소. 최대 길이는 255자로 제한.
    @Column(length = 255)
    private String address;

    // 체크인 날짜 (예: 호텔 체크인 날짜)
    private LocalDate checkInDate;

    // 체크아웃 날짜 (예: 호텔 체크아웃 날짜)
    private LocalDate checkOutDate;

    // 숙박 정보에 대한 상세 설명. 데이터베이스에서는 TEXT 타입으로 저장.
    @Column(columnDefinition = "TEXT")
    private String description;
}
