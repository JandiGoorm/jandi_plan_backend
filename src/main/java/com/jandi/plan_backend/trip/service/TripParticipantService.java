package com.jandi.plan_backend.trip.service;

import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.trip.entity.TripParticipant;
import com.jandi.plan_backend.trip.repository.TripParticipantRepository;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripParticipantService {

    private final TripParticipantRepository tripParticipantRepository;
    private final UserRepository userRepository;
    private final ValidationUtil validationUtil; // validateTripExists() 메서드가 구현되어 있다고 가정

    /**
     * 여행 계획(Trip)에 동반자를 추가합니다.
     * @param tripId             여행 계획 ID
     * @param participantUserName 동반자로 추가할 사용자의 닉네임
     * @param role               역할 (예: "동반자", "리더" 등)
     * @return 추가된 TripParticipant 엔티티
     */
    public TripParticipant addParticipant(Integer tripId, String participantUserName, String role) {
        // 여행 계획 존재 여부 검증 (ValidationUtil에 구현되어 있다고 가정)
        Trip trip = validationUtil.validateTripExists(tripId);
        // userRepository에서 userName으로 사용자 조회
        User participant = userRepository.findByUserName(participantUserName)
                .orElseThrow(() -> new RuntimeException("User not found with userName: " + participantUserName));

        TripParticipant tripParticipant = new TripParticipant();
        tripParticipant.setTrip(trip);
        tripParticipant.setParticipant(participant);
        tripParticipant.setRole(role);
        tripParticipant.setCreatedAt(LocalDateTime.now());
        return tripParticipantRepository.save(tripParticipant);
    }

    /**
     * 특정 여행 계획의 동반자 목록을 조회합니다.
     * @param tripId 여행 계획 ID
     * @return 동반자 목록
     */
    public List<TripParticipant> getParticipants(Integer tripId) {
        return tripParticipantRepository.findByTrip_TripId(tripId);
    }

    /**
     * 특정 여행 계획에서 동반자를 삭제합니다.
     * @param tripId            여행 계획 ID
     * @param participantUserName 삭제할 동반자 사용자 닉네임
     */
    public void removeParticipant(Integer tripId, String participantUserName) {
        tripParticipantRepository.deleteByTrip_TripIdAndParticipant_UserName(tripId, participantUserName);
    }
}
