package com.jandi.plan_backend.tripPlan.trip.service;

import com.jandi.plan_backend.tripPlan.trip.dto.TripParticipantRespDTO;
import com.jandi.plan_backend.tripPlan.trip.entity.Trip;
import com.jandi.plan_backend.tripPlan.trip.entity.TripParticipant;
import com.jandi.plan_backend.tripPlan.trip.repository.TripParticipantRepository;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
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
        // 1) Trip, User 검증
        Trip trip = validationUtil.validateTripExists(tripId);
        User participant = userRepository.findByUserName(participantUserName)
                .orElseThrow(() -> new RuntimeException(participantUserName + "사용자를 찾을 수 없습니다."));

        // 2) 이미 동반자로 등록되어 있는지 확인
        boolean alreadyParticipant = tripParticipantRepository
                .findByTrip_TripIdAndParticipant_UserName(tripId, participantUserName)
                .stream() // Optional이 아니라 List면 stream() 전환
                .findAny() // List => Optional
                .isPresent(); // 있으면 true

        // (또는 .isEmpty() 체크도 가능)

        if (alreadyParticipant) {
            // 중복 등록 불가 → 400 에러
            throw new BadRequestExceptionMessage("이미 동반자로 등록된 사용자입니다.");
        }

        // 3) 신규 동반자 등록
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
