package com.jandi.plan_backend.itinerary.repository;

import com.jandi.plan_backend.itinerary.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
