package com.jandi.plan_backend.itinerary.service;

import com.jandi.plan_backend.itinerary.dto.ReservationReqDTO;
import com.jandi.plan_backend.itinerary.dto.ReservationRespDTO;
import com.jandi.plan_backend.itinerary.entity.Reservation;
import com.jandi.plan_backend.itinerary.repository.ReservationRepository;
import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
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

    /** 예약 조회 */
    public Map<String, Object> getReservation(String userEmail, Integer tripId) {
        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        // 여행 계획 검증
        Trip trip = validationUtil.validateTripExists(tripId);
        validationUtil.validateUserIsAuthorOfTrip(user, trip);

        List<Reservation> allReservations = reservationRepository.findByTrip_TripId(tripId);

        // 카테고리별 비용 합계
        Map<String, Integer> cost = allReservations.stream()
                .collect(Collectors.groupingBy(
                        reservation -> reservation.getCategory().name(),
                        Collectors.summingInt(Reservation::getCost)
                ));
        // 카테고리별 예약 정보
        Map<String, List<ReservationRespDTO>> data = allReservations.stream()
                .map((Reservation reservation) -> new ReservationRespDTO(reservation, true))
                .collect(Collectors.groupingBy(ReservationRespDTO::getCategory));


        return Map.of(
                "cost", cost,
                "data", data
        );
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
        return new ReservationRespDTO(reservation, false);
    }

    /** 예약 수정 */
    public ReservationRespDTO updateReservation(String userEmail, Integer reservationId, ReservationReqDTO reservedDTO) {
        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        // 예약 검증
        Reservation reservation = validationUtil.validateReservationExists(reservationId.longValue());
        validationUtil.validateUserIsAuthorOfTrip(user, reservation.getTrip());

        log.info("reservedDTO: {}", reservedDTO);

        // 예약 수정
        if(reservedDTO.getCategory()!=null) reservation.setCategory(reservedDTO.getCategoryEnum());
        if(reservedDTO.getTitle()!=null) reservation.setTitle(reservedDTO.getTitle());
        if(reservedDTO.getDescription()!=null) reservation.setDescription(reservedDTO.getDescription());
        if(reservedDTO.getCost()!=null) reservation.setCost(reservedDTO.getCost());

        // 저장 및 반환
        reservationRepository.save(reservation);
        return new ReservationRespDTO(reservation, false);
    }
}
