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

public interface TripLikeRepository extends JpaRepository<TripLike, TripLikeId> {
    //특정 유저가 특정 여행계획을 좋아요했는지 검색
    Optional<TripLike> findByTripAndUser(Trip trip, User user);

    //특정 유저가 좋아요한 여행 계획 조회
    Page<Object> findByUser(User user, Pageable pageable);

    List<TripLike> findByUser(User user);

    //특정 유저가 좋아요한 여행 계획 갯수 반환
    long countByUser(User user);

    Optional<Object> findByTripAndUser_Email(Trip trip, String userEmail);
}
