package com.jandi.plan_backend.tripPlan.trip.repository;

import com.jandi.plan_backend.tripPlan.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.City;
import com.jandi.plan_backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

    /**
     * User, City 연관 엔티티를 함께 조회 (N+1 방지)
     */
    @EntityGraph(attributePaths = {"user", "city", "city.country"})
    Page<Trip> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "city", "city.country"})
    Optional<Trip> findByTripId(Integer tripId);

    long countByUser(User user);

    @EntityGraph(attributePaths = {"user", "city", "city.country"})
    Page<Trip> findByUser(User user, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "city", "city.country"})
    Page<Trip> findByPrivatePlan(Boolean privatePlan, Pageable pageable);

    long countByPrivatePlan(boolean b);

    @EntityGraph(attributePaths = {"user", "city", "city.country"})
    List<Trip> findTop10ByPrivatePlanFalseOrderByLikeCountDesc();

    boolean existsByCity(City city);

    /**
     * 특정 조건으로 (공개 플랜 or 해당 유저의 플랜) 검색
     */
    @EntityGraph(attributePaths = {"user", "city", "city.country"})
    Page<Trip> findByPrivatePlanOrUser(boolean b, User user, Pageable pageable);

    @Query("SELECT t FROM Trip t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Trip> searchByTitleContainingIgnoreCase(String keyword);

    @Query("SELECT t FROM Trip t JOIN t.city c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Trip> searchByCityNameContainingIgnoreCase(String keyword);

    List<Trip> findByUser(User user);

    // 공개 플랜이거나 본인의 플랜이거나 동반자로 등록된 플랜의 갯수 반환
    @Query("SELECT COUNT(t) FROM Trip t WHERE t.privatePlan = false OR t.user = :user OR t.tripId IN " +
            "(SELECT tp.trip.tripId FROM TripParticipant tp WHERE tp.participant = :user)")
    long countVisibleTrips(User user);

    // 공개 플랜이거나 본인의 플랜이거나 동반자로 등록된 플랜 목록 반환 (N+1 방지를 위한 JOIN FETCH)
    @Query("SELECT DISTINCT t FROM Trip t " +
            "LEFT JOIN FETCH t.user " +
            "LEFT JOIN FETCH t.city c " +
            "LEFT JOIN FETCH c.country " +
            "WHERE t.privatePlan = false OR t.user = :user OR t.tripId IN " +
            "(SELECT tp.trip.tripId FROM TripParticipant tp WHERE tp.participant = :user)")
    Page<Trip> findVisibleTrips(User user, Pageable pageable);

}
