package com.jandi.plan_backend.entity.resource;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 광고 정보 테이블 (advertise)
 *
 * advertise_id (PK)
 * created_at
 * title
 * image_url
 * link_url
 */
@Entity
@Table(name = "advertise")
@Data
public class Advertise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer advertiseId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 255)
    private String title;

    @Column(length = 255)
    private String imageUrl;

    @Column(length = 255)
    private String linkUrl;
}
