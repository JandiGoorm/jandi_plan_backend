package com.jandi.plan_backend.itinerary.repository;

import com.jandi.plan_backend.itinerary.entity.Reservation;
import com.jandi.plan_backend.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // 특정 여행 계획에 속한 예약 정보 조회
    List<Reservation> findByTrip_TripId(Integer tripId);

    // 특정 예약 정보 반환
    Optional<Reservation> findByReservationId(Long reservationId);

    List<Reservation> findByTrip(Trip trip);
}
