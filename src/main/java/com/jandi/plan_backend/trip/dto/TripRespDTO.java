package com.jandi.plan_backend.trip.dto;

import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.User;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 여행 계획 목록/기본 조회 DTO
 */
@Getter
public class TripRespDTO {

    private final UserTripDTO user;
    private final Integer tripId;
    private final String title;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Integer likeCount;
    private final Integer budget;
    private final Integer cityId;
    private final String cityName;
    private final String countryName;
    private final Boolean privatePlan;
    private final Double latitude;
    private final Double longitude;
    private final String cityImageUrl;
    private final String tripImageUrl;

    public TripRespDTO(User user,
                       String userProfileUrl,
                       Trip trip,
                       String cityImageUrl) {
        this(user, userProfileUrl, trip, cityImageUrl, null);
    }

    public TripRespDTO(User user,
                       String userProfileUrl,
                       Trip trip,
                       String cityImageUrl,
                       String tripImageUrl) {
        this.user = new UserTripDTO(user.getUserId(), user.getUserName(), userProfileUrl);
        this.tripId = trip.getTripId();
        this.title = trip.getTitle();
        this.startDate = trip.getStartDate();
        this.endDate = trip.getEndDate();
        this.likeCount = trip.getLikeCount();
        this.budget = trip.getBudget();
        this.cityId = trip.getCity().getCityId();
        this.cityName = trip.getCity().getName();
        this.countryName = trip.getCity().getCountry().getName();
        this.privatePlan = trip.getPrivatePlan();
        this.latitude = trip.getCity().getLatitude();
        this.longitude = trip.getCity().getLongitude();
        this.cityImageUrl = cityImageUrl;
        this.tripImageUrl = tripImageUrl;
    }

    public TripRespDTO(TripRespDTO other) {
        this.user = other.getUser();
        this.tripId = other.getTripId();
        this.title = other.getTitle();
        this.startDate = other.getStartDate();
        this.endDate = other.getEndDate();
        this.likeCount = other.getLikeCount();
        this.budget = other.getBudget();
        this.cityId = other.getCityId();
        this.cityName = other.getCityName();
        this.countryName = other.getCountryName();
        this.privatePlan = other.getPrivatePlan();
        this.latitude = other.getLatitude();
        this.longitude = other.getLongitude();
        this.cityImageUrl = other.getCityImageUrl();
        this.tripImageUrl = other.getTripImageUrl();
    }
}
