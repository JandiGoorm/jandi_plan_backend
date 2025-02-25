package com.jandi.plan_backend.user.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Continent 엔티티 클래스
 * 대륙 정보를 DB에 저장하기 위한 클래스임.
 */
@Entity
@Table(name = "continent")
@Data
public class Continent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer continentId;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @Column(length = 255)
    private String imageUrl;

    @Column(nullable = false)
    private Integer searchCount = 0;
}
