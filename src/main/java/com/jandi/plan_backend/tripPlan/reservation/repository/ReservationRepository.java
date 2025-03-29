package com.jandi.plan_backend.tripPlan.reservation.repository;

import com.jandi.plan_backend.tripPlan.reservation.entitiy.Reservation;
import com.jandi.plan_backend.tripPlan.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByTrip_TripId(Integer tripId);
    Optional<Reservation> findByReservationId(Long reservationId);
    List<Reservation> findByTrip(Trip trip);
}
