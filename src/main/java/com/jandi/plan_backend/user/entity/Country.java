package com.jandi.plan_backend.user.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Country 엔티티.
 * 이 클래스는 나라 정보를 DB에 저장하기 위한 엔티티임.
 * 각 나라(Country)는 하나의 대륙(Continent)에 속하며, 여러 나라가 하나의 대륙에 속할 수 있음.
 */
@Entity
@Table(name = "country")
@Data
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer countryId;

    @ManyToOne
    @JoinColumn(name = "continent_id", nullable = false)
    private Continent continent;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer searchCount = 0;
}
