package com.jandi.plan_backend.googlePlace.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "place_recommendation",
        uniqueConstraints = @UniqueConstraint(columnNames = {"place_id"}))
@Data
public class PlaceRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="place_id", length = 255, nullable = false)
    private String placeId;

    @Column(name="name", length = 255)
    private String name;

    @Column(name="country", length = 255)
    private String country;

    @Column(name="city", length = 255)
    private String city;

    @Column(name="address", length = 1024)
    private String address;

    @Column(name="detail_url", length = 1024)
    private String detailUrl;

    @Column(name="photo_url", length = 1024) // 길이 확장
    private String photoUrl;

    @Column(name="open_time_json", columnDefinition = "TEXT") // TEXT로 변경
    private String openTimeJson;

    private Double latitude;
    private Double longitude;
    private Double rating;
    private Integer ratingCount;
    private Boolean dineIn;
}
