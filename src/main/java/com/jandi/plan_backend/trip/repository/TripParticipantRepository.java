package com.jandi.plan_backend.trip.repository;

import com.jandi.plan_backend.trip.entity.TripParticipant;
import com.jandi.plan_backend.trip.entity.TripParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TripParticipantRepository extends JpaRepository<TripParticipant, TripParticipantId> {
    List<TripParticipant> findByTrip_TripId(Integer tripId);

    // 동반자 삭제를 위해 tripId와 participant의 userName으로 삭제하는 메서드 추가
    void deleteByTrip_TripIdAndParticipant_UserName(Integer tripId, String userName);
}
