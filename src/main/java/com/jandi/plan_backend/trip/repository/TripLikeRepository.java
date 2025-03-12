package com.jandi.plan_backend.trip.repository;

import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.trip.entity.TripLike;
import com.jandi.plan_backend.trip.entity.TripLikeId;
import com.jandi.plan_backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 여행 계획 좋아요 관련 레포지토리
 */
public interface TripLikeRepository extends JpaRepository<TripLike, TripLikeId> {

    Optional<TripLike> findByTripAndUser(Trip trip, User user);

    Page<Object> findByUser(User user, Pageable pageable);

    List<TripLike> findByUser(User user);

    long countByUser(User user);

    Optional<Object> findByTripAndUser_Email(Trip trip, String userEmail);
}
