package com.jandi.plan_backend.trip.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.jandi.plan_backend.user.entity.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 엔티티 클래스. "trip" 테이블에 매핑됨.
 * 여행 계획 정보를 저장함.
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
 * - likeCount: 여행 계획의 좋아요 수, null 불가
 * 기존에는 대표 이미지 URL을 저장했으나, 이제 이미지 정보는 Image 엔티티를 통해 관리합니다.
 */
@Entity
@Table(name = "trip")
@Data
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tripId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean privatePlan;

    @Column(nullable = false)
    private Integer likeCount;

    public Trip(User user, String title, String description, Boolean privatePlan,
                LocalDate startDate, LocalDate endDate) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        this.updatedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        this.privatePlan = privatePlan;
        this.likeCount = 0;
    }

    public Trip() {

    }
}
