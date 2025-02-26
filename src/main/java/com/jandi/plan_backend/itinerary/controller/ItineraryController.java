package com.jandi.plan_backend.itinerary.controller;

import com.jandi.plan_backend.itinerary.dto.ItineraryReqDTO;
import com.jandi.plan_backend.itinerary.dto.ItineraryRespDTO;
import com.jandi.plan_backend.itinerary.service.ItineraryService;
import com.jandi.plan_backend.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trip/itinerary")
public class ItineraryController {
    private final ItineraryService itineraryService;
    private final JwtTokenProvider jwtTokenProvider;

    public ItineraryController(ItineraryService itineraryService, JwtTokenProvider jwtTokenProvider) {
        this.itineraryService = itineraryService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 특정 여행 계획에 속한 모든 일정 조회
     * - 공개된 여행 계획은 토큰 없이 조회 가능
     * - 비공개 여행 계획은 액세스토큰이 있어야 본인만 조회 가능
     */
    @GetMapping("/{tripId}")
    public ResponseEntity<?> getItineraries(
            @PathVariable Integer tripId,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        String userEmail = null;
        if (token != null) {
            String jwtToken = token.replace("Bearer ", "");
            userEmail = jwtTokenProvider.getEmail(jwtToken);
        }
        List<ItineraryRespDTO> itineraryList = itineraryService.getItineraries(userEmail, tripId);
        return ResponseEntity.ok(itineraryList);
    }

    /**
     * 특정 여행 계획에 새로운 일정 추가 (작성자 본인만 가능)
     */
    @PostMapping("/{tripId}")
    public ResponseEntity<ItineraryRespDTO> createItinerary(
            @PathVariable Integer tripId,
            @RequestHeader("Authorization") String token,
            @RequestBody ItineraryReqDTO itineraryReqDTO
    ) {
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);
        ItineraryRespDTO savedItinerary = itineraryService.createItinerary(userEmail, tripId, itineraryReqDTO);
        return ResponseEntity.ok(savedItinerary);
    }

    /**
     * 일정 수정
     */
    @PatchMapping("/{itineraryId}")
    public ResponseEntity<ItineraryRespDTO> updateItinerary(
            @PathVariable Long itineraryId,
            @RequestHeader("Authorization") String token,
            @RequestBody ItineraryReqDTO itineraryReqDTO
    ) {
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);
        ItineraryRespDTO updatedItinerary = itineraryService.updateItinerary(userEmail, itineraryId, itineraryReqDTO);
        return ResponseEntity.ok(updatedItinerary);
    }

    /**
     * 일정 삭제
     */
    @DeleteMapping("/{itineraryId}")
    public ResponseEntity<?> deleteItinerary(
            @PathVariable Long itineraryId,
            @RequestHeader("Authorization") String token
    ) {
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);
        boolean isDeleted = itineraryService.deleteItinerary(userEmail, itineraryId);
        return isDeleted ? ResponseEntity.ok("일정이 삭제되었습니다.") : ResponseEntity.badRequest().build();
    }
}
