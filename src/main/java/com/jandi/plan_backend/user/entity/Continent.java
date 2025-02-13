package com.jandi.plan_backend.user.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "continent")
@Data
public class Continent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer continentId;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer searchCount = 0;
}
