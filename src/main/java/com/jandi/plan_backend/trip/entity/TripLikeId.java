package com.jandi.plan_backend.trip.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * trip_like 테이블 복합키
 */
@Data
public class TripLikeId implements Serializable {
    private Integer trip;
    private Integer user;
}
