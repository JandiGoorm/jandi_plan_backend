package com.jandi.plan_backend.tripPlan.trip.entity;

import lombok.Data;
import java.io.Serializable;

/**
 * 복합 키: trip_id + user_id
 */
@Data
public class TripLikeId implements Serializable {
    private Integer trip;
    private Integer user;
}
