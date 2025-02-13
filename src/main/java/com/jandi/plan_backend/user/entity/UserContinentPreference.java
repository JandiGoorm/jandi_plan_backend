package com.jandi.plan_backend.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 사용자 선호 대륙 정보를 저장하는 엔티티.
 * 이 엔티티는 사용자가 선호하는 대륙 정보를 기록하기 위한 테이블(user_continent_preference)에 매핑됨.
 *
 * 복합 기본키를 사용하며, 기본키는 사용자(user)와 대륙(continent)로 구성됨.
 * - 사용자(user)는 외래 키(user_id)를 통해 User 엔티티와 연관됨.
 * - 대륙(continent)은 외래 키(continent_id)를 통해 Continent 엔티티와 연관됨.
 *
 * 추가로, 선호 정보가 생성된 시각(createdAt)도 기록함.
 */
@Entity
@Table(name = "user_continent_preference")
@IdClass(UserContinentPreferenceId.class) // 복합 기본키 클래스로 UserContinentPreferenceId를 사용
@Data
public class UserContinentPreference {

    /**
     * 사용자 엔티티.
     * - 이 필드는 기본키의 일부로 사용됨.
     * - 테이블의 "user_id" 컬럼과 매핑되며, null 값을 허용하지 않음.
     */
    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 대륙 엔티티.
     * - 이 필드는 기본키의 일부로 사용됨.
     * - 테이블의 "continent_id" 컬럼과 매핑되며, null 값을 허용하지 않음.
     */
    @Id
    @ManyToOne
    @JoinColumn(name = "continent_id", nullable = false)
    private Continent continent;

    /**
     * 선호 정보가 생성된 시각.
     * - 레코드가 생성될 때의 시각을 저장함.
     * - null 값을 허용하지 않음.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
