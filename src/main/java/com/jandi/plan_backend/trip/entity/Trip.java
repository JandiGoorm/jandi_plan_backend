package com.jandi.plan_backend.trip.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.jandi.plan_backend.user.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 엔티티 클래스. "trip" 테이블에 매핑됨.
 * 여행 계획 정보를 저장함.
 *
 * 컬럼:
 * - trip_id: 기본키, 자동 증가
 * - user_id: 사용자와의 다대일 관계 (FK)
 * - title: 여행 계획 제목, 최대 255자, null 불가
 * - description: 여행 계획 설명, TEXT 타입
 * - start_date: 여행 시작 날짜, null 불가
 * - end_date: 여행 종료 날짜, null 불가
 * - created_at: 생성 시각, null 불가
 * - updated_at: 수정 시각, null 불가
 * - privatePlan: 여행 계획이 비공개인지 여부, boolean 타입, null 불가
 */
@Entity
@Table(name = "trip")
@Data
public class Trip {

    // trip 테이블의 기본키. 값은 데이터베이스에서 자동으로 생성됨.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tripId;

    // 사용자와의 다대일 관계를 나타냄. 여러 여행 계획은 하나의 사용자에 속함.
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 여행 계획 제목. 최대 길이는 255자로 제한되며 null 값 허용 안 함.
    @Column(nullable = false, length = 255)
    private String title;

    // 여행 계획 설명. 길이에 제한 없이 TEXT 타입으로 저장.
    @Column(columnDefinition = "TEXT")
    private String description;

    // 여행 시작 날짜. LocalDate 타입, null 값 허용 안 함.
    @Column(nullable = false)
    private LocalDate startDate;

    // 여행 종료 날짜. LocalDate 타입, null 값 허용 안 함.
    @Column(nullable = false)
    private LocalDate endDate;

    // 여행 계획이 생성된 시각. LocalDateTime 타입, null 값 허용 안 함.
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 여행 계획이 마지막으로 수정된 시각. LocalDateTime 타입, null 값 허용 안 함.
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 여행 계획의 공개 여부. true이면 비공개, false이면 공개.
    // 컬럼명은 "privatePlan"으로 사용하며 null 값 허용 안 함.
    @Column(nullable = false)
    private Boolean privatePlan;

    // 여행 계획의 좋아요 수. null 값은 허용되지 않음.
    @Column(nullable = false)
    private Integer likeCount;

    // 여행 계획의 대표 이미지.
    @Column(length = 255)
    private String imageUrl;
}
