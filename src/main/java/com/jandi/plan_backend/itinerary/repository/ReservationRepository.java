package com.jandi.plan_backend.itinerary.repository;

import com.jandi.plan_backend.itinerary.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // 특정 여행 계획에 속한 예약 정보 조회
    List<Reservation> findByTrip_TripId(Integer tripId);
}
