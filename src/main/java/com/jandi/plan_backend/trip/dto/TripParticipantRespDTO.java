package com.jandi.plan_backend.trip.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 동반자 정보 DTO
 */
@Data
public class TripParticipantRespDTO {
    private Integer tripId;
    private Integer participantUserId;
    private String participantUserName;
    private String role;
    private LocalDateTime createdAt;

    public TripParticipantRespDTO(Integer tripId,
                                  Integer participantUserId,
                                  String participantUserName,
                                  String role,
                                  LocalDateTime createdAt) {
        this.tripId = tripId;
        this.participantUserId = participantUserId;
        this.participantUserName = participantUserName;
        this.role = role;
        this.createdAt = createdAt;
    }
}
