package com.jandi.plan_backend.tripPlan.itinerary.repository;

import com.jandi.plan_backend.tripPlan.itinerary.entity.Itinerary;
import com.jandi.plan_backend.tripPlan.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {
    List<Itinerary> findByTrip_TripId(Integer tripId);
    List<Itinerary> findByTrip(Trip trip);
}
