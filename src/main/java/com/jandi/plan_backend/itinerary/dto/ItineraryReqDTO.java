package com.jandi.plan_backend.itinerary.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ItineraryReqDTO {
    // 일정 등록/수정을 위한 요청 DTO
    private Long placeId;      // 장소 ID
    private String date;       // 일정 날짜 (YYYY-MM-DD)
    private String startTime;  // 시작 시간 (HH:mm:ss 또는 HH:mm)
    private String title;      // 일정 제목
    private Integer cost;      // 비용

    public ItineraryReqDTO(Long placeId, String date, String startTime, String title, Integer cost) {
        this.placeId = placeId;
        this.date = date;
        this.startTime = startTime;
        this.title = title;
        this.cost = cost;
    }
}
