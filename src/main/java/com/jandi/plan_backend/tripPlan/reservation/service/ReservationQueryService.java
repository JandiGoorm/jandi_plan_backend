package com.jandi.plan_backend.tripPlan.reservation.service;

import com.jandi.plan_backend.tripPlan.reservation.dto.ReservationRespDTO;
import com.jandi.plan_backend.tripPlan.reservation.entitiy.Reservation;
import com.jandi.plan_backend.tripPlan.reservation.repository.ReservationRepository;
import com.jandi.plan_backend.tripPlan.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.util.TripUtil;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationQueryService {
    private final ValidationUtil validationUtil;
    private final TripUtil tripUtil;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    public Map<String, Object> getReservation(String userEmail, Integer tripId) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        Trip trip = validationUtil.validateTripExists(tripId);

        // 접근 권한 검증
        boolean canViewTrip = tripUtil.isCanViewTrip(trip, user);
        if (!canViewTrip) {
            throw new BadRequestExceptionMessage("비공개 여행 계획입니다");
        }

        // 예약 정보 가져오기
        List<Reservation> reservations = reservationRepository.findByTrip_TripId(tripId);
        Map<String, List<ReservationRespDTO>> data = organizeGroupByCategory(reservations);
        Map<String, Integer> cost = calculateGroupByCategory(reservations);

        return Map.of(
                "cost", cost,
                "data", data
        );
    }

    // 예약 정보를 카테고리에 따라 정리
    private Map<String, List<ReservationRespDTO>> organizeGroupByCategory(List<Reservation> reservations) {
        return reservations.stream()
                .map(r -> new ReservationRespDTO(r, true))
                .collect(Collectors.groupingBy(ReservationRespDTO::getCategory));
    }

    // 예약 정보에서 카테고리별/전체 합산 비용 추출
    private Map<String, Integer> calculateGroupByCategory(List<Reservation> reservations) {
        // 카테고리별 비용 합산
        Map<String, Integer> cost = reservations.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCategory().name(),
                        Collectors.summingInt(Reservation::getCost)
                ));

        // 전체 비용 추가
        int totalCost = cost.values().stream().mapToInt(Integer::intValue).sum();
        cost.put("TOTAL", totalCost);

        return cost;
    }


}
