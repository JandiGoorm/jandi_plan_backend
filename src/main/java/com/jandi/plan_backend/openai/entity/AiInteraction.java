package com.jandi.plan_backend.openai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ai_interaction")
public class AiInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 예: "restaurant" (맛집 관련 질의), "travel" (여행지 질의) 등
    @Column(nullable = false, length = 50)
    private String queryType;

    // AI에게 보낸 프롬프트
    @Column(columnDefinition = "TEXT")
    private String prompt;

    // AI의 원본 JSON 응답
    @Column(columnDefinition = "TEXT")
    private String rawResponse;

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
