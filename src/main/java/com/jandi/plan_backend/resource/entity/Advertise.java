package com.jandi.plan_backend.resource.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 광고 정보 테이블(Advertise)을 나타내는 엔티티.
 *
 * 이 클래스는 데이터베이스의 "advertise" 테이블과 매핑된다.
 *
 * 각 필드의 역할:
 * - advertiseId: 광고의 고유 식별자. 기본 키(PK)로 사용되며, 데이터베이스에서 자동 증가 전략(IDENTITY)을 사용해 값이 생성됨.
 * - createdAt: 광고가 생성된 시각을 저장. null이 될 수 없으며, 광고가 만들어진 날짜와 시간을 기록함.
 * - title: 광고의 제목을 저장하는 문자열 필드. 최대 길이는 255자로 제한됨.
 * - imageUrl: 광고에 사용된 이미지의 URL을 저장하는 문자열 필드. 최대 길이는 255자로 제한됨.
 * - linkUrl: 광고를 클릭할 때 이동하는 URL을 저장하는 문자열 필드. 최대 길이는 255자로 제한됨.
 *
 * Lombok의 @Data 어노테이션은 자동으로 getter, setter, equals, hashCode, toString 메서드를 생성함.
 */
@Entity
@Table(name = "advertise")
@Data
public class Advertise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer advertiseId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 255)
    private String title;

    @Column(length = 255)
    private String imageUrl;

    @Column(length = 255)
    private String linkUrl;
}
