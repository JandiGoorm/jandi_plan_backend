package com.jandi.plan_backend.openai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ai_restaurant")
public class AiRestaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어느 AI 상호작용에서 비롯된 데이터인지 추적
    @ManyToOne
    @JoinColumn(name = "interaction_id")
    private AiInteraction interaction;

    // city_id (도시 테이블과의 매핑)
    private Integer cityId;

    // 맛집 정보
    @Column(length = 255, nullable = false)
    private String name;            // 식당 이름

    private double latitude;        // 위도
    private double longitude;       // 경도
    private double rating;          // 평점(5점 만점)

    @Column(columnDefinition = "TEXT")
    private String description;     // 상세 설명 (메뉴, 특징 등)

    @Column(length = 500)
    private String imageUrl;        // 이미지 URL

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
