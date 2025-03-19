package com.jandi.plan_backend.trip.repository;

import com.jandi.plan_backend.trip.entity.TripParticipant;
import com.jandi.plan_backend.trip.entity.TripParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 여행 계획 동반자 관련 레포지토리
 */
public interface TripParticipantRepository extends JpaRepository<TripParticipant, TripParticipantId> {

    List<TripParticipant> findByTrip_TripId(Integer tripId);

    void deleteByTrip_TripIdAndParticipant_UserName(Integer tripId, String userName);

    Optional<TripParticipant> findByTrip_TripIdAndParticipant_UserName(Integer tripId, String userName);

    Optional<TripParticipant> findByTrip_TripIdAndParticipant_UserId(Integer tripId, Integer userId);

}
