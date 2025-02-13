package com.jandi.plan_backend.user.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "major_destination")
@Data
public class MajorDestination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer destinationId;

    /**
     * 나라(Country)와 다대일 관계 (여러 여행지가 하나의 나라에 속함)
     */
    @ManyToOne
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String imageUrl;

    @Column(nullable = false)
    private Integer searchCount = 0;
}
