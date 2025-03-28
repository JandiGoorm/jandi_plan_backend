package com.jandi.plan_backend.tripPlan.reservation.service;

import com.jandi.plan_backend.tripPlan.reservation.dto.ReservationReqDTO;
import com.jandi.plan_backend.tripPlan.reservation.dto.ReservationRespDTO;
import com.jandi.plan_backend.tripPlan.reservation.entitiy.Reservation;
import com.jandi.plan_backend.tripPlan.reservation.repository.ReservationRepository;
import com.jandi.plan_backend.tripPlan.trip.entity.Trip;
import com.jandi.plan_backend.tripPlan.trip.entity.TripParticipant;
import com.jandi.plan_backend.tripPlan.trip.repository.TripParticipantRepository;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.util.TripUtil;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ValidationUtil validationUtil;
    private final TripUtil tripUtil;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public Map<String, Object> getReservation(String userEmail, Integer tripId) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        Trip trip = validationUtil.validateTripExists(tripId);

        // 접근 권한 검증
        boolean canViewTrip = tripUtil.isCanViewTrip(trip, user);
        if (!canViewTrip) {
            throw new BadRequestExceptionMessage("비공개 여행 계획입니다");
        }

        // 예약 정보 가져오기
        List<Reservation> allReservations = reservationRepository.findByTrip_TripId(tripId);

        // 예약 정보를 카테고리별 그룹핑
        Map<String, List<ReservationRespDTO>> data = allReservations.stream()
                .map(r -> new ReservationRespDTO(r, true))
                .collect(Collectors.groupingBy(ReservationRespDTO::getCategory));

        // 카테고리별 비용 합산
        Map<String, Integer> cost = allReservations.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCategory().name(),
                        Collectors.summingInt(Reservation::getCost)
                ));

        // 전체 비용 계산
        int totalCost = cost.values().stream().mapToInt(Integer::intValue).sum();
        cost.put("TOTAL", totalCost);

        return Map.of(
                "cost", cost,
                "data", data
        );
    }

    public ReservationRespDTO createReservation(String userEmail, Integer tripId, ReservationReqDTO reservedDTO) {
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        Trip trip = validationUtil.validateTripExists(tripId);

        // 변경 권한 검증
        tripUtil.isCanEditTrip(trip, user);

        Reservation reservation = new Reservation();
        reservation.setTrip(trip);
        reservation.setCategory(reservedDTO.getCategoryEnum());
        reservation.setTitle(reservedDTO.getTitle());
        reservation.setDescription(reservedDTO.getDescription());
        reservation.setCost(reservedDTO.getCost());

        reservationRepository.save(reservation);
        return new ReservationRespDTO(reservation, false);
    }

    public ReservationRespDTO updateReservation(String userEmail, Integer reservationId, ReservationReqDTO reservedDTO) {
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        Reservation reservation = validationUtil.validateReservationExists(reservationId.longValue());
        Trip trip = reservation.getTrip();

        // 변경 권한 검증
        tripUtil.isCanEditTrip(trip, user);

        if (reservedDTO.getCategory() != null) reservation.setCategory(reservedDTO.getCategoryEnum());
        if (reservedDTO.getTitle() != null) reservation.setTitle(reservedDTO.getTitle());
        if (reservedDTO.getDescription() != null) reservation.setDescription(reservedDTO.getDescription());
        if (reservedDTO.getCost() != null) reservation.setCost(reservedDTO.getCost());

        reservationRepository.save(reservation);
        return new ReservationRespDTO(reservation, false);
    }

    public boolean deleteReservation(String userEmail, Integer reservationId) {
        User user = validationUtil.validateUserExists(userEmail);
        Reservation reservation = validationUtil.validateReservationExists(reservationId.longValue());
        Trip trip = reservation.getTrip();

        // 변경 권한 검증
        tripUtil.isCanEditTrip(trip, user);

        reservationRepository.delete(reservation);
        return true;
    }
}
