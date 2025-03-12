package com.jandi.plan_backend.trip.dto;

import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.User;
import lombok.Getter;

/**
 * 여행 계획 단일 조회 시, 비공개 여부와 liked 여부까지 포함
 */
@Getter
public class TripItemRespDTO extends TripRespDTO {

    private final Boolean privatePlan;
    private final Boolean liked;

    public TripItemRespDTO(User user,
                           String userProfileUrl,
                           Trip trip,
                           String cityImageUrl,
                           Boolean liked) {
        super(user, userProfileUrl, trip, cityImageUrl);
        this.privatePlan = trip.getPrivatePlan();
        this.liked = liked;
    }

    public TripItemRespDTO(User user,
                           String userProfileUrl,
                           Trip trip,
                           String cityImageUrl,
                           String tripImageUrl,
                           Boolean liked) {
        super(user, userProfileUrl, trip, cityImageUrl, tripImageUrl);
        this.privatePlan = trip.getPrivatePlan();
        this.liked = liked;
    }

    public TripItemRespDTO(TripRespDTO tripRespDTO, Boolean liked) {
        super(tripRespDTO);
        this.privatePlan = tripRespDTO.getPrivatePlan();
        this.liked = liked;
    }
}
