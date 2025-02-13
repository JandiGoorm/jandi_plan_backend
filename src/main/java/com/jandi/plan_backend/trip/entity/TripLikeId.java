package com.jandi.plan_backend.trip.entity;

import lombok.Data;
import java.io.Serializable;

/**
 * trip_like 테이블의 복합 기본키를 표현하는 클래스.
 *
 * 이 클래스는 trip_like 엔티티에서 두 개의 컬럼(trip_id와 user_id)을 결합하여 복합 키로 사용하기 위해 만듦.
 * JPA에서 복합 키를 사용할 때, 식별자 클래스는 반드시 Serializable 인터페이스를 구현해야 함.
 *
 * 필드 설명:
 * - trip: trip_like 테이블의 trip_id 컬럼에 해당하는 값.
 * - user: trip_like 테이블의 user_id 컬럼에 해당하는 값.
 *
 * Lombok의 @Data 어노테이션을 통해 getter, setter, equals, hashCode, toString 메서드를 자동 생성함.
 * equals와 hashCode 메서드는 복합 키 객체의 동등성 비교에 중요함.
 */
@Data
public class TripLikeId implements Serializable {
    private Integer trip;
    private Integer user;
}
