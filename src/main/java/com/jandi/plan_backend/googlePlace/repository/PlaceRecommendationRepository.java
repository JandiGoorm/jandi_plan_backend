package com.jandi.plan_backend.googlePlace.repository;

import com.jandi.plan_backend.googlePlace.entity.PlaceRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaceRecommendationRepository extends JpaRepository<PlaceRecommendation, Long> {
    Optional<PlaceRecommendation> findByPlaceId(String placeId);
}
