package com.jandi.plan_backend.util;

import com.jandi.plan_backend.tripPlan.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TripUtil {
    private final ValidationUtil validationUtil;

    public boolean isCanViewTrip(Trip trip, User user) {
        /// 공개 여행 계획인 경우 모두 접근 가능
        if(!trip.getPrivatePlan())
            return true;

        /// 비공개 여행 계획인 경우
        // 미 로그인 유저: 접근 불가
        if(user == null) return false;

        // 계획의 소유자: 접근 가능
        if(isTripOwner(trip, user)) return true;

        // 계획의 동반자: 접근 가능
        if(isTripParticipant(trip, user)) return true;

        // 그외: 관리자 혹은 스텝만 접근 가능
        boolean isAdmin = validationUtil.validateUserIsAdmin(user);
        boolean isStaff = validationUtil.validateUserIsStaff(user);
        return isAdmin || isStaff;
    }

    public void isCanEditTrip(Trip trip, User user) {
        // 미 로그인 유저: 수정 불가
        if(user == null)
            throw new BadRequestExceptionMessage("로그인이 필요합니다");

        // 계획의 소유자: 수정 가능
        if(isTripOwner(trip, user)) return;

        // 계획의 동반자: 수정 가능
        if(isTripParticipant(trip, user)) return;

        // 그 외: 수정 불가
        throw new BadRequestExceptionMessage("해당 작업을 수행할 권한이 없습니다");
    }

    public boolean isTripOwner(Trip trip, User user) {
        Integer tripOwner = trip.getUser().getUserId();
        return user.getUserId().equals(tripOwner);
    }

    public boolean isTripParticipant(Trip trip, User user) {
        return trip.getParticipants() != null && trip.getParticipants().stream()
                .anyMatch(tp -> tp.getParticipant().getUserId().equals(user.getUserId()));
    }
}
