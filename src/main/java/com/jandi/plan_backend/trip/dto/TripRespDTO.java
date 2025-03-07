package com.jandi.plan_backend.trip.dto;

import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.User;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 여행 계획 조회 시 반환할 DTO
 * - tripImageUrl: 사용자가 직접 업로드한 여행계획 이미지 (있으면 표시)
 * - cityImageUrl: 도시 이미지
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

    // 도시 이미지
    private final String cityImageUrl;
    // 여행 계획 전용 이미지 (사용자가 업로드)
    private final String tripImageUrl;

    /**
     * 4개 인자 버전 (기존 코드와 호환)
     * tripImageUrl = null
     */
    public TripRespDTO(User user,
                       String userProfileUrl,
                       Trip trip,
                       String cityImageUrl) {
        this(user, userProfileUrl, trip, cityImageUrl, null);
    }

    /**
     * 5개 인자 버전 (새로운 로직: tripImageUrl 추가)
     */
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

    public TripRespDTO(TripRespDTO tripRespDTO) {
        this.user = tripRespDTO.getUser();
        this.tripId = tripRespDTO.getTripId();
        this.title = tripRespDTO.getTitle();
        this.startDate = tripRespDTO.getStartDate();
        this.endDate = tripRespDTO.getEndDate();
        this.likeCount = tripRespDTO.getLikeCount();
        this.budget = tripRespDTO.getBudget();
        this.cityId = tripRespDTO.getCityId();
        this.cityName = tripRespDTO.getCityName();
        this.countryName = tripRespDTO.getCountryName();
        this.privatePlan = tripRespDTO.getPrivatePlan();

        this.latitude = tripRespDTO.getLatitude();
        this.longitude = tripRespDTO.getLongitude();
        this.cityImageUrl = tripRespDTO.getCityImageUrl();

        this.tripImageUrl = tripRespDTO.getTripImageUrl();
    }
}
