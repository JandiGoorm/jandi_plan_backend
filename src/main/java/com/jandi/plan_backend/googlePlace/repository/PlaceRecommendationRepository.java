package com.jandi.plan_backend.googlePlace.repository;

import com.jandi.plan_backend.googlePlace.entity.PlaceRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PlaceRecommendationRepository extends JpaRepository<PlaceRecommendation, Long> {
    List<PlaceRecommendation> findByCountryAndCity(String country, String city);
    void deleteByCountryAndCity(String country, String city);
    void deleteByCountryAndCityAndCreatedAtBefore(String country, String city, LocalDateTime threshold);
    List<PlaceRecommendation> findByCountryAndCityAndCreatedAtAfter(String country, String city, LocalDateTime createdAt);
    Optional<PlaceRecommendation> findByPlaceId(String placeId);
}
