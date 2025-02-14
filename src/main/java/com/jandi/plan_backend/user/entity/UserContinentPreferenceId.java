package com.jandi.plan_backend.user.entity;

import lombok.Data;
import java.io.Serializable;

/**
 * 복합 기본키 클래스.
 * user_continent_preference 테이블에서 사용되는 복합키를 정의함.
 * 이 키는 UserContinentPreference 엔티티에서 사용자와 대륙을 식별하는 데 사용됨.
 *
 * Serializable을 구현해야 JPA에서 복합키 클래스로 인식함.
 *
 * 필드:
 * - user: 사용자의 기본키 값을 저장함. (User 엔티티의 ID)
 * - continent: 대륙의 기본키 값을 저장함. (Continent 엔티티의 ID)
 */
@Data
public class UserContinentPreferenceId implements Serializable {
    // User 엔티티의 기본키 (정수형)
    private Integer user;

    // Continent 엔티티의 기본키 (정수형)
    private Integer continent;
}
