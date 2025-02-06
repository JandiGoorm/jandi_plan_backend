package com.jandi.plan_backend.user.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 대륙별 주요 여행지 테이블 (major_destination)
 *
 * destination_id (PK, auto-increment)
 * continent_id (FK → continent.continent_id)
 * name
 * description
 * image_url
 */
@Entity
@Table(name = "major_destination")
@Data
public class MajorDestination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer destinationId;

    @ManyToOne
    @JoinColumn(name = "continent_id", nullable = false)
    private Continent continent;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String imageUrl;
}
