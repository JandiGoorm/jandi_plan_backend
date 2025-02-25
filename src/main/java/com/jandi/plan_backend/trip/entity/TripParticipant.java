package com.jandi.plan_backend.trip.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.jandi.plan_backend.user.entity.User;
import java.time.LocalDateTime;

/**
 * 여행 동반자(참여자) 정보를 저장하는 테이블(trip_participant)과 매핑됨.
 */
@Entity
@Table(name = "trip_participant")
@IdClass(TripParticipantId.class)
@Data
public class TripParticipant {

    @Id
    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Id
    @ManyToOne
    @JoinColumn(name = "participant_user_id", nullable = false)
    private User participant;

    @Column(length = 50)
    private String role;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
