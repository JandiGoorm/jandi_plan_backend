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

    /**
     * 통계 레코드의 기본 식별자.
     * 단 하나의 레코드만 존재하므로, 일반적으로 id 값은 1로 고정해서 사용.
     */
    @Id
    private Integer id;

    /**
     * 대륙 검색 횟수를 저장.
     * null 값은 허용하지 않으며, 기본값은 0.
     */
    @Column(nullable = false)
    private Integer continentSearchCount = 0;

    /**
     * 국가 검색 횟수를 저장.
     * null 값은 허용하지 않으며, 기본값은 0.
     */
    @Column(nullable = false)
    private Integer countrySearchCount = 0;

    /**
     * 여행지 검색 횟수를 저장.
     * null 값은 허용하지 않으며, 기본값은 0.
     */
    @Column(nullable = false)
    private Integer destinationSearchCount = 0;

    /**
     * 게시글 생성 횟수를 저장.
     * null 값은 허용하지 않으며, 기본값은 0.
     */
    @Column(nullable = false)
    private Integer postCreationCount = 0;

    /**
     * 여행 생성 횟수를 저장.
     * null 값은 허용하지 않으며, 기본값은 0.
     */
    @Column(nullable = false)
    private Integer tripCreationCount = 0;

    /**
     * 마지막 업데이트 시간을 저장.
     * null 값은 허용하지 않으며, 기본값은 객체 생성 시의 현재 시간.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * 엔티티가 저장되기 전에 호출됨.
     * 저장 시 updatedAt 필드를 현재 시간으로 갱신.
     */
    @PrePersist
    public void prePersist() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 엔티티가 수정되기 전에 호출됨.
     * 업데이트 시 updatedAt 필드를 현재 시간으로 갱신.
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
