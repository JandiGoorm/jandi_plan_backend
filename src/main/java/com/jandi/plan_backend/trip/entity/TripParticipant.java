package com.jandi.plan_backend.trip.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.jandi.plan_backend.user.entity.User;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

/**
 * 여행 동반자(참여자) 테이블( trip_participant ) 엔티티.
 *
 * 이 엔티티는 여행(trip)과 사용자의 참여 정보를 관리한다.
 * 복합 기본키는 TripParticipantId 클래스를 통해 정의되며,
 * 두 개의 외래키(trip_id, participant_user_id)를 조합하여 고유 식별자로 사용한다.
 *
 * 필드 설명:
 * - trip: 해당 참여자가 속한 여행 정보를 참조한다.
 *         ManyToOne 관계로, 여러 참여자가 하나의 여행에 속할 수 있다.
 * - participant: 참여자로 등록된 사용자를 나타낸다.
 *                ManyToOne 관계로, 한 사용자가 여러 여행에 참여할 수 있다.
 * - role: 참여자의 역할이나 지위를 나타내는 문자열 (최대 50자).
 * - createdAt: 참여 정보가 생성된 시간을 기록하는 필드.
 *
 * Lombok의 @Data 어노테이션은 getter, setter, equals, hashCode, toString 메서드를 자동으로 생성한다.
 */
@Entity
@Table(name = "trip_participant")
@IdClass(TripParticipantId.class) // 복합 키를 정의하는 식별자 클래스를 지정
@Data
public class TripParticipant {

    // 여행 정보에 대한 외래키, 복합 기본키의 일부로 사용.
    @Id
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    // 참여 사용자에 대한 외래키, 복합 기본키의 일부로 사용.
    @Id
    @ManyToOne
    @JoinColumn(name = "participant_user_id", nullable = false)
    private User participant;

    // 참여자의 역할을 나타내는 컬럼. (예: '관리자', '참여자' 등)
    @Column(length = 50)
    private String role;

    // 이 참여 기록이 생성된 시간을 저장.
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
