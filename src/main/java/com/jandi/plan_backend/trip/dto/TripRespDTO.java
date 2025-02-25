package com.jandi.plan_backend.trip.dto;

import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.User;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class TripRespDTO {
    private final UserTripDTO user;
    private final Integer tripId;
    private final String title;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String description;
    private final Integer likeCount;
    private final String imageUrl;

    public TripRespDTO(UserTripDTO user, Integer tripId, String title, LocalDate startDate, LocalDate endDate,
                       String description, Integer likeCount, String imageUrl) {
        this.user = user;
        this.tripId = tripId;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.likeCount = likeCount;
        this.imageUrl = imageUrl;
    }

    public TripRespDTO(User user, String userProfileUrl, Trip trip, String TripImageUrl){
        this.user = new UserTripDTO(user.getUserId(), user.getUserName(), userProfileUrl);
        this.tripId = trip.getTripId();
        this.title = trip.getTitle();
        this.startDate = trip.getStartDate();
        this.endDate = trip.getEndDate();
        this.description = trip.getDescription();
        this.likeCount = trip.getLikeCount();
        this.imageUrl = TripImageUrl;
    }
}
