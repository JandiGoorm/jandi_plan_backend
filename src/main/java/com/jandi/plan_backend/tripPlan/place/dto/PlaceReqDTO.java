package com.jandi.plan_backend.tripPlan.place.dto;

import lombok.Data;

@Data
public class PlaceReqDTO {
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
}
