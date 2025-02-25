package com.jandi.plan_backend.trip.repository;

import com.jandi.plan_backend.trip.entity.TripLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripLikeRepository extends JpaRepository<TripLike, Long> {
}
