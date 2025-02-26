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
    private final Integer likeCount;
    private final String imageUrl;
    private final Integer budget;
    private final Integer cityId;

    public TripRespDTO(UserTripDTO user, Integer tripId, String title, LocalDate startDate, LocalDate endDate,
                       Integer likeCount, String imageUrl, Integer budget, Integer cityId) {
        this.user = user;
        this.tripId = tripId;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.likeCount = likeCount;
        this.imageUrl = imageUrl;
        this.budget = budget;
        this.cityId = cityId;
    }

    public TripRespDTO(User user, String userProfileUrl, Trip trip, String tripImageUrl) {
        this.user = new UserTripDTO(user.getUserId(), user.getUserName(), userProfileUrl);
        this.tripId = trip.getTripId();
        this.title = trip.getTitle();
        this.startDate = trip.getStartDate();
        this.endDate = trip.getEndDate();
        this.likeCount = trip.getLikeCount();
        this.imageUrl = tripImageUrl;
        this.budget = trip.getBudget();
        this.cityId = trip.getCity().getCityId();
    }
}
