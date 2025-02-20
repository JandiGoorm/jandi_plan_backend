package com.jandi.plan_backend.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 사용자 선호 나라 정보를 저장하는 엔티티.
 *
 * 이 엔티티는 사용자가 선호하는 나라 정보를 기록하기 위한 테이블(user_country_preference)에 매핑된다.
 * 복합 기본키는 사용자(User)와 나라(Country)로 구성되며,
 * 각각 외래 키(user_id, country_id)를 통해 User와 Country 엔티티와 연관된다.
 *
 * 추가로, 선호 정보가 생성된 시각(createdAt)을 기록한다.
 */
@Entity
@Table(name = "user_country_preference")
@Data
@IdClass(UserCountryPreferenceId.class)
public class UserCountryPreference {

    /**
     * 사용자 엔티티.
     * - 복합 기본키의 일부로 사용.
     * - 테이블의 "user_id" 컬럼과 매핑되며, null 값은 허용되지 않음.
     */
    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 나라 엔티티.
     * - 복합 기본키의 일부로 사용.
     * - 테이블의 "country_id" 컬럼과 매핑되며, null 값은 허용되지 않음.
     */
    @Id
    @ManyToOne
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    /**
     * 선호 정보가 생성된 시각.
     * - 레코드 생성 시의 시각을 기록.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
