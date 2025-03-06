package com.jandi.plan_backend.trip.controller;

import com.jandi.plan_backend.trip.entity.TripParticipant;
import com.jandi.plan_backend.trip.service.TripParticipantService;
import com.jandi.plan_backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trip")
@RequiredArgsConstructor
public class TripParticipantController {

    private final TripParticipantService tripParticipantService;
    private final JwtTokenProvider jwtTokenProvider; // 필요시 JWT 토큰을 이용한 추가 검증 가능

    /**
     * 여행 계획의 동반자 추가 API
     * POST /api/trip/{tripId}/participants
     *
     * @param tripId             여행 계획 ID
     * @param participantUserName 동반자로 추가할 사용자의 닉네임
     * @param role               역할 (옵션, 기본값 "동반자")
     * @return 추가된 동반자 정보
     */
    @PostMapping("/{tripId}/participants")
    public ResponseEntity<?> addParticipant(
            @PathVariable Integer tripId,
            @RequestHeader("Authorization") String token,  // 필요 시 JWT 검증 로직 추가
            @RequestParam String participantUserName,
            @RequestParam(required = false, defaultValue = "동반자") String role
    ) {
        TripParticipant participant = tripParticipantService.addParticipant(tripId, participantUserName, role);
        return ResponseEntity.ok(participant);
    }

    /**
     * 여행 계획의 동반자 목록 조회 API
     * GET /api/trip/{tripId}/participants
     *
     * @param tripId 여행 계획 ID
     * @return 동반자 목록
     */
    @GetMapping("/{tripId}/participants")
    public ResponseEntity<?> getParticipants(@PathVariable Integer tripId) {
        List<TripParticipant> participants = tripParticipantService.getParticipants(tripId);
        return ResponseEntity.ok(participants);
    }

    /**
     * 여행 계획의 동반자 삭제 API
     * DELETE /api/trip/{tripId}/participants/{participantUserName}
     *
     * @param tripId             여행 계획 ID
     * @param participantUserName 삭제할 동반자 사용자 닉네임
     * @return 삭제 결과 메시지
     */
    @DeleteMapping("/{tripId}/participants/{participantUserName}")
    public ResponseEntity<?> removeParticipant(
            @PathVariable Integer tripId,
            @PathVariable String participantUserName
    ) {
        tripParticipantService.removeParticipant(tripId, participantUserName);
        return ResponseEntity.ok("동반자가 삭제되었습니다.");
    }
}
