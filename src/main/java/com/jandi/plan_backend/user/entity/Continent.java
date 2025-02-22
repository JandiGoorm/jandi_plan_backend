package com.jandi.plan_backend.user.entity;

import jakarta.persistence.*; // JPA 관련 어노테이션들을 사용하기 위해 임포트
import lombok.Data;

/**
 * Continent 엔티티 클래스
 * 대륙 정보를 DB에 저장하기 위한 클래스임.
 */
@Entity // 이 클래스가 JPA 엔티티임을 표시
@Table(name = "continent") // 매핑되는 테이블 이름을 "continent"로 지정
@Data // Lombok의 @Data 어노테이션으로 getter, setter, toString, equals, hashCode 메서드 자동 생성
public class Continent {

    /**
     * 대륙의 고유 ID.
     * 기본 키이며, DB에서 자동 증가(IDENTITY 전략)로 값이 할당됨.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer continentId;

    /**
     * 대륙의 이름.
     * null 값이 들어갈 수 없고, 길이는 최대 50자로 제한되며, 중복된 이름은 허용되지 않음.
     */
    @Column(nullable = false, length = 50, unique = true)
    private String name;

    /**
     * 주요 여행지의 이미지 URL.
     * 최대 길이는 255자로 제한됨.
     */
    @Column(length = 255)
    private String imageUrl;

    /**
     * 대륙 검색 횟수.
     * 검색 시마다 이 값이 증가할 것으로 예상하며, 기본값은 0임.
     */
    @Column(nullable = false)
    private Integer searchCount = 0;
}
