package com.jandi.plan_backend.tripPlan.place.dto;

import lombok.Data;

@Data
public class PlaceRespDTO {
    private Long placeId;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;

    public PlaceRespDTO(Long placeId, String name, String address, Double latitude, Double longitude) {
        this.placeId = placeId;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
