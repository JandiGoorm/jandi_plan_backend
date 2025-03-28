package com.jandi.plan_backend.tripPlan.place.repository;

import com.jandi.plan_backend.tripPlan.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {
}
