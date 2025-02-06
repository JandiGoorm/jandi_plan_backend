package com.jandi.plan_backend.user.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 대륙 목록 테이블 (continent)
 *
 * continent_id (PK, auto-increment)
 * name (대륙 이름, 예: 유럽, 아시아 등)
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
}
