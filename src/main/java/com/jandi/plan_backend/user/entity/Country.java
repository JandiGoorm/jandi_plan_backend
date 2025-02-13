package com.jandi.plan_backend.user.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "country")
@Data
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer countryId;

    /**
     * 대륙(Continent)과 다대일 관계 (여러 나라가 하나의 대륙에 속함)
     */
    @ManyToOne
    @JoinColumn(name = "continent_id", nullable = false)
    private Continent continent;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer searchCount = 0;
}
