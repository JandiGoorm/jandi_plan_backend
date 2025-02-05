package com.jandi.plan_backend.entity.trip;

import lombok.Data;

import java.io.Serializable;

/**
 * trip_participant 테이블 복합키
 */
@Data
public class TripParticipantId implements Serializable {
    private Integer trip;
    private Integer participant;
}
