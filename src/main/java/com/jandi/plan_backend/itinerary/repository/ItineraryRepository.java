package com.jandi.plan_backend.itinerary.repository;

import com.jandi.plan_backend.itinerary.entity.Itinerary;
import com.jandi.plan_backend.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {
    // 특정 여행(tripId)에 속한 모든 일정을 조회
    List<Itinerary> findByTrip_TripId(Integer tripId);

    List<Itinerary> findByTrip(Trip trip);
}
