package com.jandi.plan_backend.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "statistics")
@Data
public class Statistics {

    /**
     * 단 하나의 row만 사용 (예: id=1)
     */
    @Id
    private Integer id;

    @Column(nullable = false)
    private Integer continentSearchCount = 0;

    @Column(nullable = false)
    private Integer countrySearchCount = 0;

    @Column(nullable = false)
    private Integer destinationSearchCount = 0;

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
