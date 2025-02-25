package com.jandi.plan_backend.image.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "image")
@Data
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer imageId;

    @Column(nullable = false, length = 50)
    private String targetType;

    @Column(nullable = false)
    private Integer targetId;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false, length = 100)
    private String owner;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
