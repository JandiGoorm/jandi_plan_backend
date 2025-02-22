package com.jandi.plan_backend.trip.dto;

import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.User;
import lombok.Getter;

import java.time.LocalDate;

/** 여행 플랜 관련 조회 시 */
// user 관련: userId, userName, profileImageUrl
// plan 관련: tripId, title, description, date(startDate, endDate),
// 넘겨야 할 정보: 작성자, 작성자 프로필, 날짜(시작/종료), 좋아요 수, 제목, 설명, 대표 이미지
@Getter
public class TripRespDTO {
    private UserTripDTO user;

    private Integer tripId;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer likeCount;
    private String imageUrl;

    public TripRespDTO(Trip trip, ImageService imageService) {
        this.user = new UserTripDTO(trip.getUser(), imageService);

        this.tripId = trip.getTripId();
        this.title = trip.getTitle();
        this.startDate = trip.getStartDate();
        this.endDate = trip.getEndDate();
        this.likeCount = trip.getLikeCount();
        this.imageUrl = trip.getImageUrl();
    }
}
