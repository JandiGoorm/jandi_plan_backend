package com.jandi.plan_backend.itinerary.dto;

import com.jandi.plan_backend.itinerary.entity.ReservationCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationReqDTO {
    private String category;
    private String title;
    private String description;
    private Integer cost;

    public ReservationReqDTO(String category, String title, String description, Integer cost) {
        this.category = category;
        this.title = title;
        this.description = description;
        this.cost = cost;
    }

    public ReservationCategory getCategoryEnum() {
        return ReservationCategory.fromDisplayName(category); // 한글 → ENUM 변환
    }
}
