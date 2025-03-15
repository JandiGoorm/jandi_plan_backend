package com.jandi.plan_backend.googlePlace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommPlaceRespDTO {
    private String placeId;
    private String name;
    private String url;
    private double rating;
    private String photoUrl;
    private String address;
    private double latitude;
    private double longitude;
    private int ratingCount;
    private boolean dineIn;
    private String openTimeJson;
    private String country;
    private String city;
}
