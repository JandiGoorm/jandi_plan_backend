package com.jandi.plan_backend.tripPlan.reservation.dto;

import com.jandi.plan_backend.tripPlan.reservation.entitiy.Reservation;
import lombok.Data;

@Data
public class ReservationRespDTO {
    private Long reservationId;
    private String category;
    private String title;
    private String description;
    private Integer cost;

    public ReservationRespDTO(Reservation reservation, boolean isEnglishCategory) {
        this.reservationId = reservation.getReservationId();
        this.category = isEnglishCategory
                ? reservation.getCategory().name()
                : reservation.getCategory().getDisplayName();
        this.title = reservation.getTitle();
        this.description = reservation.getDescription();
        this.cost = reservation.getCost();
    }
}
