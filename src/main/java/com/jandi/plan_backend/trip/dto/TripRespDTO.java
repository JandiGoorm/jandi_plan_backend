package com.jandi.plan_backend.trip.dto;

import lombok.Getter;
import java.time.LocalDate;

/**
 * 여행 계획 정보를 전달하기 위한 순수 DTO.
 * 작성자 정보, 여행 계획의 ID, 제목, 시작/종료 날짜, 설명, 좋아요 수, 대표 이미지 공개 URL을 포함합니다.
 */
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
}
