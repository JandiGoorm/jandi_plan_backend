package com.jandi.plan_backend.trip.dto;

import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.User;
import lombok.Getter;

/**
 * 여행 계획 단일 조회 전용 DTO
 * TripRespDTO를 상속하여 privatePlan, liked 등 추가 정보를 포함
 */
@Getter
public class TripItemRespDTO extends TripRespDTO {
    private final Boolean privatePlan;
    private final Boolean liked;

    // 5개 인자 생성자 (tripImageUrl = null)
    public TripItemRespDTO(User user,
                           String userProfileUrl,
                           Trip trip,
                           String cityImageUrl,
                           Boolean liked
    ) {
        super(user, userProfileUrl, trip, cityImageUrl);
        this.privatePlan = trip.getPrivatePlan();
        this.liked = liked;
    }

    // 6개 인자 생성자 (tripImageUrl 포함)
    public TripItemRespDTO(User user,
                           String userProfileUrl,
                           Trip trip,
                           String cityImageUrl,
                           String tripImageUrl,
                           Boolean liked
    ) {
        super(user, userProfileUrl, trip, cityImageUrl, tripImageUrl);
        this.privatePlan = trip.getPrivatePlan();
        this.liked = liked;
    }
}
