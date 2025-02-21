package com.jandi.plan_backend.storage.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 서비스 내 모든 이미지 정보를 저장하는 엔티티.
 *
 * 이 클래스는 데이터베이스의 "image" 테이블과 매핑된다.
 *
 * 필드 설명:
 * - imageId:
 *   이미지의 고유 식별자(PK). 자동 증가(IDENTITY) 전략을 사용.
 *
 * - targetType:
 *   이미지가 속하는 대상(예: "userProfile", "advertise", "notice", "community", "banner" 등).
 *
 * - targetId:
 *   이미지가 연결된 대상 엔티티의 식별자. (대상에 따라 값이 달라짐)
 *
 * - imageUrl:
 *   실제 이미지 파일의 URL 또는 경로.
 *
 * - altText:
 *   이미지 로드 실패 시 대체 텍스트.
 *
 * - owner:
 *   이미지 업로더의 식별자(이메일).
 *
 * - createdAt:
 *   이미지 생성 시각.
 *
 * Lombok의 @Data가 getter, setter, toString, equals, hashCode를 자동 생성.
 */
@Entity
@Table(name = "image")
@Data
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer imageId;

    @Column(nullable = false, length = 50)
    private String targetType;

    @Column(nullable = false)
    private Integer targetId;

    @Column(nullable = false, length = 255)
    private String imageUrl;

    @Column(nullable = false, length = 100)
    private String owner;  // 이메일로 저장

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
