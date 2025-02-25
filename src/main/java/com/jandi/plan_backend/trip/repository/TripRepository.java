package com.jandi.plan_backend.trip.repository;

import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

    // 유저가 작성한 여행 계획의 수 반환
    long countByUser(User user);

    // 해당 유저의 여행 목록을 페이지네이션하여 가져오기 위한 메서드
    Page<Object> findByUser(User user, Pageable pageable);

    // 공개된 여행 목록만 페이지네이션하여 가져오기 위한 메서드
    Page<Object> findByPrivatePlan(Boolean privatePlan, Pageable pageable);

    // 공개/비공개된 여행 계획의 수 반환
    long countByPrivatePlan(boolean b);

    // 공개된 여행 계획 중 좋아요 수가 많은 상위 10개 조회
    List<Trip> findTop10ByPrivatePlanFalseOrderByLikeCountDesc();
}
