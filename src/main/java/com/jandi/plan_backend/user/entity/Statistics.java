package com.jandi.plan_backend.user.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 통계 정보를 저장하는 엔티티.
 * 통계 데이터는 단 하나의 row (예: id = 1)만 사용함.
 */
@Entity
@Table(name = "statistics")
@Data
public class Statistics {

    @Id
    private Integer id;

    @Column(nullable = false)
    private Integer continentSearchCount = 0;

    @Column(nullable = false)
    private Integer countrySearchCount = 0;

    @Column(nullable = false)
    private Integer citySearchCount = 0;

    @Column(nullable = false)
    private Integer postCreationCount = 0;

    @Column(nullable = false)
    private Integer tripCreationCount = 0;

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
