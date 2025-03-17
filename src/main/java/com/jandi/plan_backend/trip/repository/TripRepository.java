package com.jandi.plan_backend.trip.repository;

import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.City;
import com.jandi.plan_backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

    long countByUser(User user);

    Page<Trip> findByUser(User user, Pageable pageable);

    Page<Trip> findByPrivatePlan(Boolean privatePlan, Pageable pageable);

    long countByPrivatePlan(boolean b);

    List<Trip> findTop10ByPrivatePlanFalseOrderByLikeCountDesc();

    boolean existsByCity(City city);

    /**
     * 특정 조건으로 (공개 플랜 or 해당 유저의 플랜) 검색
     */
    Page<Trip> findByPrivatePlanOrUser(boolean b, User user, Pageable pageable);

    @Query("SELECT t FROM Trip t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Trip> searchByTitleContainingIgnoreCase(String keyword);

    @Query("SELECT t FROM Trip t JOIN t.city c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Trip> searchByCityNameContainingIgnoreCase(String keyword);

    List<Trip> findByUser(User user);
}
