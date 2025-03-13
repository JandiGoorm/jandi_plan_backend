package com.jandi.plan_backend.googlePlace.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "place_recommendation")
public class PlaceRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_id", length = 128, nullable = false)
    private String placeId;

    @Column(length = 255)
    private String name;

    @Column(name = "detail_url", length = 1024)
    private String detailUrl;

    @Column(length = 1024)
    private String photoUrl;

    @Column(length = 255)
    private String address;

    private double latitude;
    private double longitude;
    private double rating;
    private int ratingCount;
    private boolean dineIn; // 1이면 매장 식사 가능, 0이면 매장 식사 불가능

    @Column(name = "open_time_json", columnDefinition = "TEXT")
    private String openTimeJson;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String city;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
