package com.jandi.plan_backend.user.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Country 엔티티.
 * 이 클래스는 나라 정보를 DB에 저장하기 위한 엔티티임.
 * 각 나라(Country)는 하나의 대륙(Continent)에 속하며, 여러 나라가 하나의 대륙에 속할 수 있음.
 */
@Entity // JPA 엔티티임을 표시
@Table(name = "country") // DB 테이블 이름을 "country"로 지정
@Data // Lombok의 @Data 어노테이션: getter, setter, toString, equals, hashCode 자동 생성
public class Country {

    /**
     * 나라의 고유 식별자.
     * 기본 키이며, DB에서 자동 증가(IDENTITY 전략)를 사용해 값을 할당함.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer countryId;

    /**
     * 나라와 대륙 간의 다대일 관계.
     * 여러 나라가 하나의 대륙(Continent)에 속함.
     * DB에는 "continent_id"라는 외래키 열이 생성되며, null 값은 허용하지 않음.
     */
    @ManyToOne
    @JoinColumn(name = "continent_id", nullable = false)
    private Continent continent;

    /**
     * 나라의 이름.
     * null 값은 허용되지 않으며, 최대 길이는 100자로 제한됨.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 나라의 검색 횟수.
     * 기본값은 0으로 설정되어 있으며, 나라 검색 시마다 이 값이 증가할 것으로 예상됨.
     */
    @Column(nullable = false)
    private Integer searchCount = 0;
}
