package com.jandi.plan_backend.entity.resource;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 서비스 내 모든 이미지 정보 테이블 (image)
 *
 * image_id (PK)
 * target_type (user, advertise, notice, community 등)
 * target_id (실제 대상 레코드의 PK)
 * image_url (이미지 경로 또는 URL)
 * alt_text (대체 텍스트)
 * created_at
 */
@Entity
@Table(name = "image")
@Data
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer imageId;

    // 예: "user", "advertise", "notice", "community"
    @Column(nullable = false, length = 50)
    private String targetType;

    // 대상 엔티티의 PK 값
    @Column(nullable = false)
    private Integer targetId;

    // 실제 이미지 파일의 URL
    @Column(nullable = false, length = 255)
    private String imageUrl;

    @Column(length = 255)
    private String altText;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
