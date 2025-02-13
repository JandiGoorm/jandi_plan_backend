package com.jandi.plan_backend.trip.entity;

import lombok.Data;
import java.io.Serializable;

/**
 * trip_participant 테이블의 복합키 클래스.
 *
 * 이 클래스는 trip_participant 테이블에서 기본키로 사용되는
 * 두 개의 외래키(trip, participant)를 조합해 고유 식별자로 사용된다.
 *
 * - trip: Trip 엔티티의 기본 키를 나타내며, 해당 여행을 식별하는 값.
 * - participant: User 엔티티의 기본 키를 나타내며, 여행에 참여하는 사용자를 식별하는 값.
 *
 * Lombok의 @Data 어노테이션을 사용해 getter, setter, equals, hashCode, toString 메서드를 자동으로 생성한다.
 *
 * Serializable 인터페이스를 구현하여 객체 직렬화를 지원한다.
 */
@Data
public class TripParticipantId implements Serializable {
    private Integer trip;
    private Integer participant;
}
