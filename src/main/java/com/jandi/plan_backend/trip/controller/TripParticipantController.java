package com.jandi.plan_backend.trip.controller;

import com.jandi.plan_backend.trip.dto.TripParticipantRespDTO;
import com.jandi.plan_backend.trip.service.TripParticipantService;
import com.jandi.plan_backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 여행 계획 동반자 관련 API
 */
@RestController
@RequestMapping("/api/trip")
@RequiredArgsConstructor
public class TripParticipantController {

    private final TripParticipantService tripParticipantService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 동반자 추가
     */
    @PostMapping("/{tripId}/participants")
    public ResponseEntity<TripParticipantRespDTO> addParticipant(
            @PathVariable Integer tripId,
            @RequestHeader("Authorization") String token,
            @RequestParam String participantUserName,
            @RequestParam(required = false, defaultValue = "동반자") String role
    ) {
        // 토큰 검증이 필요한 경우 추가
        TripParticipantRespDTO dto = tripParticipantService.addParticipant(tripId, participantUserName, role);
        return ResponseEntity.ok(dto);
    }

    /**
     * 동반자 목록 조회
     */
    @GetMapping("/{tripId}/participants")
    public ResponseEntity<List<TripParticipantRespDTO>> getParticipants(@PathVariable Integer tripId) {
        List<TripParticipantRespDTO> dtos = tripParticipantService.getParticipants(tripId);
        return ResponseEntity.ok(dtos);
    }

    /**
     * 동반자 삭제
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
