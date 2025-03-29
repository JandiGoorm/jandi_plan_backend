package com.jandi.plan_backend.tripPlan.reservation.entitiy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ReservationCategory {
    TRANSPORTATION("교통편"),
    ACCOMMODATION("숙박"),
    ETC("기타");

    private final String displayName;

    ReservationCategory(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static ReservationCategory fromDisplayName(String displayName) {
        for (ReservationCategory category : values()) {
            if (category.displayName.equals(displayName)) {
                return category;
            }
        }
        throw new IllegalArgumentException("잘못된 카테고리: " + displayName);
    }
}
