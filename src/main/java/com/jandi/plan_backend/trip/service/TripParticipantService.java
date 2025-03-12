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

/**
 * 동반자 관리 로직
 */
@Service
@RequiredArgsConstructor
public class TripParticipantService {

    private final TripParticipantRepository tripParticipantRepository;
    private final UserRepository userRepository;
    private final ValidationUtil validationUtil;

    /**
     * 동반자 추가
     */
    public TripParticipantRespDTO addParticipant(Integer tripId, String participantUserName, String role) {
        Trip trip = validationUtil.validateTripExists(tripId);
        User participant = userRepository.findByUserName(participantUserName)
                .orElseThrow(() -> new RuntimeException("User not found: " + participantUserName));

        TripParticipant tp = new TripParticipant();
        tp.setTrip(trip);
        tp.setParticipant(participant);
        tp.setRole(role);
        tp.setCreatedAt(LocalDateTime.now());
        TripParticipant saved = tripParticipantRepository.save(tp);

        return convertToDTO(saved);
    }

    /**
     * 동반자 목록 조회
     */
    public List<TripParticipantRespDTO> getParticipants(Integer tripId) {
        return tripParticipantRepository.findByTrip_TripId(tripId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 동반자 삭제
     */
    @Transactional
    public void removeParticipant(Integer tripId, String participantUserName) {
        tripParticipantRepository.deleteByTrip_TripIdAndParticipant_UserName(tripId, participantUserName);
    }

    private TripParticipantRespDTO convertToDTO(TripParticipant tp) {
        return new TripParticipantRespDTO(
                tp.getTrip().getTripId(),
                tp.getParticipant().getUserId(),
                tp.getParticipant().getUserName(),
                tp.getRole(),
                tp.getCreatedAt()
        );
    }
}
