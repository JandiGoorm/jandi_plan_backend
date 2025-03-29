package com.jandi.plan_backend.tripPlan.trip.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;
import com.jandi.plan_backend.user.entity.User;

import java.time.LocalDateTime;

/**
 * 여행 계획 동반자 엔티티
 */
@Entity
@Table(name = "trip_participant")
@IdClass(TripParticipantId.class)
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
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
