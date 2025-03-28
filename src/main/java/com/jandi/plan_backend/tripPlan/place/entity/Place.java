package com.jandi.plan_backend.tripPlan.place.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "place", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "address"})
})
@Data
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long placeId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;
}
