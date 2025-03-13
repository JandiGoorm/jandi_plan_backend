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

    // 30일 이내(최근 데이터) 조회용
    List<PlaceRecommendation> findByCountryAndCityAndCreatedAtAfter(String country, String city, LocalDateTime createdAt);

    // placeId로 이미 저장된 데이터가 있는지 확인
    Optional<PlaceRecommendation> findByPlaceId(String placeId);
}
