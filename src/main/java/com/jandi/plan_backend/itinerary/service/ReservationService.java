package com.jandi.plan_backend.itinerary.service;

import com.jandi.plan_backend.itinerary.dto.ReservationReqDTO;
import com.jandi.plan_backend.itinerary.dto.ReservationRespDTO;
import com.jandi.plan_backend.itinerary.entity.Reservation;
import com.jandi.plan_backend.itinerary.repository.ReservationRepository;
import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReservationService {

    private final ValidationUtil validationUtil;
    private final ReservationRepository reservationRepository;

    public ReservationService(ValidationUtil validationUtil, ReservationRepository reservationRepository) {
        this.validationUtil = validationUtil;
        this.reservationRepository = reservationRepository;
    }

    public Map<String, Object> getReservation(String userEmail, Integer tripId) {
        User user = validationUtil.validateUserExists(userEmail);
        Trip trip = validationUtil.validateTripExists(tripId);

        List<Reservation> allReservations = reservationRepository.findByTrip_TripId(tripId);

        // 예약 정보를 카테고리별로 그룹핑
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
        // 작성자 또는 동반자 권한 검증
        validationUtil.validateUserHasEditPermission(user, trip);

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
        // 작성자 또는 동반자 권한 검증
        validationUtil.validateUserHasEditPermission(user, trip);

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
        // 작성자 또는 동반자 권한 검증
        validationUtil.validateUserHasEditPermission(user, trip);
        reservationRepository.delete(reservation);
        return true;
    }
}
