package com.jandi.plan_backend.trip.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class TripParticipantId implements Serializable {
    private Integer trip;
    private Integer participant;
}
