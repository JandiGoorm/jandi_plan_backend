package com.jandi.plan_backend.resource.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 서비스 내 모든 이미지 정보 테이블(IMAGE)을 나타내는 엔티티.
 *
 * 이 클래스는 데이터베이스의 "image" 테이블과 매핑된다.
 *
 * 각 필드의 역할은 다음과 같다:
 *
 * - imageId:
 *   - 이미지의 고유 식별자(PK).
 *   - 데이터베이스에서 자동 증가 전략(IDENTITY)을 사용해 값이 생성됨.
 *
 * - targetType:
 *   - 이미지가 어떤 대상에 속하는지 나타내는 문자열.
 *   - 예: "user", "advertise", "notice", "community" 등.
 *   - 최대 길이 50자로 제한하며 null 값을 허용하지 않음.
 *
 * - targetId:
 *   - 이미지가 연결된 대상 엔티티의 식별자(PK).
 *   - 대상 엔티티에 따라 해당 값이 다르게 사용됨.
 *   - null 값을 허용하지 않음.
 *
 * - imageUrl:
 *   - 실제 이미지 파일의 URL 또는 경로를 저장하는 필드.
 *   - 최대 길이 255자로 제한하며 null 값을 허용하지 않음.
 *
 * - altText:
 *   - 이미지가 로드되지 않을 때 대체로 보여줄 텍스트.
 *   - 선택적 필드로 최대 길이 255자로 제한됨.
 *
 * - createdAt:
 *   - 이미지가 생성된 날짜와 시간을 나타냄.
 *   - null 값을 허용하지 않음.
 *
 * Lombok의 @Data 어노테이션은 자동으로 getter, setter, toString, equals, hashCode 메서드를 생성한다.
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

    @Column(length = 255)
    private String altText;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
