package com.jandi.plan_backend.itinerary.repository;

import com.jandi.plan_backend.itinerary.entity.Reservation;
import com.jandi.plan_backend.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByTrip_TripId(Integer tripId);
    Optional<Reservation> findByReservationId(Long reservationId);
    List<Reservation> findByTrip(Trip trip);
}
