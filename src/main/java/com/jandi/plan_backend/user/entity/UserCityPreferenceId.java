package com.jandi.plan_backend.user.entity;

import lombok.Data;
import java.io.Serializable;

/**
 * user_destination_preference 테이블의 복합 기본키 클래스.
 *
 * 이 클래스는 user_destination_preference 테이블의 복합 기본키를 구성하는 두 필드를 정의함.
 * 복합 기본키는 사용자와 주요 여행지의 식별자로 구성됨.
 *
 * 필드 설명:
 * - user: 사용자 엔티티의 식별자 값. user_destination_preference 테이블의 user_id 컬럼과 매핑됨.
 * - majorDestination: 주요 여행지 엔티티의 식별자 값. user_destination_preference 테이블의 destination_id 컬럼과 매핑됨.
 *
 * 이 클래스는 Serializable 인터페이스를 구현하여, 복합 키 객체가 직렬화 될 수 있도록 함.
 */
@Data
public class UserCityPreferenceId implements Serializable {

    // 사용자 엔티티의 기본키 값 (user_id)
    private Integer user;

    // 주요 여행지 엔티티의 기본키 값 (destination_id)
    private Integer majorDestination;
}
