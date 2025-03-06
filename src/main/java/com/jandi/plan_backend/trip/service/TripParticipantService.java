package com.jandi.plan_backend.trip.service;

import com.jandi.plan_backend.trip.dto.TripParticipantRespDTO;
import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.trip.entity.TripParticipant;
import com.jandi.plan_backend.trip.repository.TripParticipantRepository;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripParticipantService {

    private final TripParticipantRepository tripParticipantRepository;
    private final UserRepository userRepository;
    private final ValidationUtil validationUtil; // validateTripExists(), validateUserExists() 등 구현 필요

    /**
     * 여행 계획(Trip)에 동반자를 추가합니다.
     * @param tripId              여행 계획 ID
     * @param participantUserName 동반자로 추가할 사용자의 닉네임
     * @param role                역할 (예: "동반자", "리더" 등)
     * @return 추가된 TripParticipant 정보를 담은 DTO
     */
    public TripParticipantRespDTO addParticipant(Integer tripId, String participantUserName, String role) {
        Trip trip = validationUtil.validateTripExists(tripId);
        User participant = userRepository.findByUserName(participantUserName)
                .orElseThrow(() -> new RuntimeException("User not found with userName: " + participantUserName));

        TripParticipant tripParticipant = new TripParticipant();
        tripParticipant.setTrip(trip);
        tripParticipant.setParticipant(participant);
        tripParticipant.setRole(role);
        tripParticipant.setCreatedAt(LocalDateTime.now());
        TripParticipant saved = tripParticipantRepository.save(tripParticipant);

        return convertToDTO(saved);
    }

    /**
     * 특정 여행 계획의 동반자 목록을 조회합니다.
     * @param tripId 여행 계획 ID
     * @return 동반자 DTO 목록
     */
    public List<TripParticipantRespDTO> getParticipants(Integer tripId) {
        List<TripParticipant> participants = tripParticipantRepository.findByTrip_TripId(tripId);
        return participants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 특정 여행 계획에서 동반자를 삭제합니다.
     * @param tripId              여행 계획 ID
     * @param participantUserName 삭제할 동반자 사용자 닉네임
     */
    @Transactional
    public void removeParticipant(Integer tripId, String participantUserName) {
        tripParticipantRepository.deleteByTrip_TripIdAndParticipant_UserName(tripId, participantUserName);
    }

    /**
     * TripParticipant 엔티티를 TripParticipantRespDTO로 변환합니다.
     */
    private TripParticipantRespDTO convertToDTO(TripParticipant tripParticipant) {
        return new TripParticipantRespDTO(
                tripParticipant.getTrip().getTripId(),
                tripParticipant.getParticipant().getUserId(),
                tripParticipant.getParticipant().getUserName(),
                tripParticipant.getRole(),
                tripParticipant.getCreatedAt()
        );
    }
}
