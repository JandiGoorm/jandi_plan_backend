package com.jandi.plan_backend.tripPlan.itinerary.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ItineraryReqDTO {
    private Long placeId;
    private String date;
    private String startTime;
    private String title;
    private Integer cost;

    public ItineraryReqDTO(Long placeId, String date, String startTime, String title, Integer cost) {
        this.placeId = placeId;
        this.date = date;
        this.startTime = startTime;
        this.title = title;
        this.cost = cost;
    }
}
