package com.jandi.plan_backend.user.entity;

import lombok.Data;
import java.io.Serializable;

/**
 * 사용자-나라 선호 복합 기본키 클래스.
 *
 * 이 클래스는 user_country_preference 테이블에서 사용자와 나라를 식별하는 복합 기본키를 정의한다.
 * 두 필드 모두 User 엔티티와 Country 엔티티의 기본키 값(정수형)을 저장한다.
 */
@Data
public class UserCountryPreferenceId implements Serializable {
    // User 엔티티의 기본키 값 (user_id)
    private Integer user;

    // Country 엔티티의 기본키 값 (country_id)
    private Integer country;
}
