package com.jandi.plan_backend.itinerary.dto;

import com.jandi.plan_backend.itinerary.entity.Reservation;
import com.jandi.plan_backend.itinerary.entity.ReservationCategory;
import lombok.Data;

@Data
public class ReservationRespDTO {
    private Long reservationId;
    private String category;
    private String title;
    private String description;
    private Integer cost;

    public ReservationRespDTO(Reservation reservation) {
        reservationId = reservation.getReservationId();
        category = reservation.getCategory().getDisplayName();
        title = reservation.getTitle();
        description = reservation.getDescription();
        cost = reservation.getCost();
    }
}
