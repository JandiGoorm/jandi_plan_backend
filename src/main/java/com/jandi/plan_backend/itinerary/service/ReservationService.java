package com.jandi.plan_backend.itinerary.service;

import com.jandi.plan_backend.itinerary.dto.ReservationReqDTO;
import com.jandi.plan_backend.itinerary.dto.ReservationRespDTO;
import com.jandi.plan_backend.itinerary.entity.Reservation;
import com.jandi.plan_backend.itinerary.repository.ReservationRepository;
import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {
    private final ValidationUtil validationUtil;
    private final ReservationRepository reservationRepository;

    public ReservationService(ValidationUtil validationUtil, ReservationRepository reservationRepository) {
        this.validationUtil = validationUtil;
        this.reservationRepository = reservationRepository;
    }

    /** 예약 생성 */
    public ReservationRespDTO createReservation(String userEmail, Integer tripId, ReservationReqDTO reservedDTO) {
        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        // 여행 계획 검증
        Trip trip = validationUtil.validateTripExists(tripId);
        validationUtil.validateUserIsAuthorOfTrip(user, trip);

        // 예약 생성
        Reservation reservation = new Reservation();
        reservation.setTrip(trip);
        reservation.setCategory(reservedDTO.getCategoryEnum());
        reservation.setTitle(reservedDTO.getTitle());
        reservation.setDescription(reservedDTO.getDescription());
        reservation.setCost(reservedDTO.getCost());

        // 저장 및 반환
        reservationRepository.save(reservation);
        return new ReservationRespDTO(reservation);
    }
}
