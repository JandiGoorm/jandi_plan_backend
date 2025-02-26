package com.jandi.plan_backend.itinerary.dto;

import com.jandi.plan_backend.itinerary.entity.Itinerary;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class ItineraryRespDTO {
    private Long itineraryId;
    private Integer tripId;
    private Long placeId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String title;
    private Integer cost;
    private LocalDate createdAt;

    public ItineraryRespDTO(Itinerary itinerary) {
        this.itineraryId = itinerary.getItineraryId();
        // trip 엔티티에서 tripId 추출 (Trip의 PK 타입이 Integer임)
        this.tripId = itinerary.getTrip().getTripId();
        this.placeId = itinerary.getPlaceId();
        this.date = itinerary.getDate();
        this.startTime = itinerary.getStartTime();
        this.endTime = itinerary.getEndTime();
        this.title = itinerary.getTitle();
        this.cost = itinerary.getCost();
        this.createdAt = itinerary.getCreatedAt();
    }
}
