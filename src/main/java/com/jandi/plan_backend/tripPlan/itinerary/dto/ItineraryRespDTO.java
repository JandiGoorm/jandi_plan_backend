package com.jandi.plan_backend.tripPlan.itinerary.dto;

import com.jandi.plan_backend.tripPlan.itinerary.entity.Itinerary;
import com.jandi.plan_backend.tripPlan.place.dto.PlaceRespDTO;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class ItineraryRespDTO {
    private Long itineraryId;
    private Integer tripId;
    private PlaceRespDTO place;
    private LocalDate date;
    private LocalTime startTime;
    private String title;
    private Integer cost;
    private LocalDate createdAt;

    public ItineraryRespDTO(Itinerary itinerary, PlaceRespDTO place) {
        this.itineraryId = itinerary.getItineraryId();
        this.tripId = itinerary.getTrip().getTripId();
        this.place = place;
        this.date = itinerary.getDate();
        this.startTime = itinerary.getStartTime();
        this.title = itinerary.getTitle();
        this.cost = itinerary.getCost();
        this.createdAt = itinerary.getCreatedAt();
    }
}
