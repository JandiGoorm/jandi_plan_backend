package com.jandi.plan_backend.user.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * MajorDestination 엔티티
 * 주요 여행지 정보를 저장하기 위한 엔티티.
 */
@Entity
@Table(name = "major_destination")
@Data
public class MajorDestination {

    /**
     * 주요 여행지의 고유 식별자.
     * 기본 키이며, 데이터베이스에서 자동 증가(IDENTITY 전략)를 사용하여 값이 할당됨.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer destinationId;

    /**
     * 주요 여행지가 속한 나라(Country)와의 다대일 관계.
     * 여러 여행지가 하나의 나라에 속함.
     * 데이터베이스 테이블에서는 외래키 "country_id"를 통해 연결됨.
     * null 값은 허용되지 않음.
     */
    @ManyToOne
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    /**
     * 주요 여행지의 이름.
     * null 값은 허용되지 않으며, 최대 길이는 100자로 제한됨.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 주요 여행지에 대한 상세 설명.
     * 긴 텍스트 저장을 위해 TEXT 타입으로 지정됨.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 주요 여행지의 이미지 URL.
     * 최대 길이는 255자로 제한됨.
     */
    @Column(length = 255)
    private String imageUrl;

    /**
     * 주요 여행지의 검색 횟수.
     * 기본값은 0이며, 검색 이벤트 발생 시마다 증가할 것으로 예상됨.
     */
    @Column(nullable = false)
    private Integer searchCount = 0;
}
