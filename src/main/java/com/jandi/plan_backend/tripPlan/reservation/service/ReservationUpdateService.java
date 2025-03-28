package com.jandi.plan_backend.tripPlan.reservation.service;

import com.jandi.plan_backend.tripPlan.reservation.dto.ReservationReqDTO;
import com.jandi.plan_backend.tripPlan.reservation.dto.ReservationRespDTO;
import com.jandi.plan_backend.tripPlan.reservation.entitiy.Reservation;
import com.jandi.plan_backend.tripPlan.reservation.repository.ReservationRepository;
import com.jandi.plan_backend.tripPlan.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.util.TripUtil;
import com.jandi.plan_backend.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationUpdateService {

    private final ValidationUtil validationUtil;
    private final TripUtil tripUtil;
    private final ReservationRepository reservationRepository;

    public ReservationRespDTO createReservation(String userEmail, Integer tripId, ReservationReqDTO reservedDTO) {
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        Trip trip = validationUtil.validateTripExists(tripId);

        // 변경 권한 검증
        tripUtil.isCanEditTrip(trip, user);

        // 예약 추가
        Reservation reservation = createReservationData(trip, reservedDTO);
        return new ReservationRespDTO(reservation, false);
    }

    public ReservationRespDTO updateReservation(String userEmail, Integer reservationId, ReservationReqDTO reservedDTO) {
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        Reservation reservation = validationUtil.validateReservationExists(reservationId.longValue());
        Trip trip = reservation.getTrip();

        // 변경 권한 검증
        tripUtil.isCanEditTrip(trip, user);

        // 예약 수정
        updateReservation(reservation, reservedDTO);
        return new ReservationRespDTO(reservation, false);
    }

    public boolean deleteReservation(String userEmail, Integer reservationId) {
        User user = validationUtil.validateUserExists(userEmail);
        Reservation reservation = validationUtil.validateReservationExists(reservationId.longValue());
        Trip trip = reservation.getTrip();

        // 변경 권한 검증
        tripUtil.isCanEditTrip(trip, user);

        // 예약 삭제
        reservationRepository.delete(reservation);
        return !reservationRepository.existsById(Long.valueOf(reservationId));
    }

    private Reservation createReservationData(Trip trip, ReservationReqDTO reservedDTO) {
        Reservation reservation = new Reservation();
        reservation.setTrip(trip);
        reservation.setCategory(reservedDTO.getCategoryEnum());
        reservation.setTitle(reservedDTO.getTitle());
        reservation.setDescription(reservedDTO.getDescription());
        reservation.setCost(reservedDTO.getCost());

        reservationRepository.save(reservation);
        return reservation;
    }

    private void updateReservation(Reservation reservation, ReservationReqDTO reservedDTO) {
        if (reservedDTO.getCategory() != null)  // 카테고리 수정
            reservation.setCategory(reservedDTO.getCategoryEnum());
        if (reservedDTO.getTitle() != null) // 제목 수정
            reservation.setTitle(reservedDTO.getTitle());
        if (reservedDTO.getDescription() != null) // 설명 수정
            reservation.setDescription(reservedDTO.getDescription());
        if (reservedDTO.getCost() != null) // 비용 수정
            reservation.setCost(reservedDTO.getCost());

        reservationRepository.save(reservation);
    }
}
