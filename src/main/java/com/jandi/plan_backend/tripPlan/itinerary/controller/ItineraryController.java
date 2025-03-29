package com.jandi.plan_backend.tripPlan.itinerary.controller;

import com.jandi.plan_backend.tripPlan.itinerary.dto.ItineraryReqDTO;
import com.jandi.plan_backend.tripPlan.itinerary.dto.ItineraryRespDTO;
import com.jandi.plan_backend.tripPlan.itinerary.service.ItineraryQueryService;
import com.jandi.plan_backend.security.JwtTokenProvider;
import com.jandi.plan_backend.tripPlan.itinerary.service.ItineraryUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trip/itinerary")
public class ItineraryController {
    private final ItineraryQueryService itineraryQueryService;
    private final ItineraryUpdateService itineraryUpdateService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/{tripId}")
    public ResponseEntity<?> getItineraries(
            @PathVariable Integer tripId,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        String userEmail = null;
        if (token != null) {
            userEmail = jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
        }
        List<ItineraryRespDTO> itineraryList = itineraryQueryService.getItineraries(userEmail, tripId);
        return ResponseEntity.ok(itineraryList);
    }

    @PostMapping("/{tripId}")
    public ResponseEntity<ItineraryRespDTO> createItinerary(
            @PathVariable Integer tripId,
            @RequestHeader("Authorization") String token,
            @RequestBody ItineraryReqDTO itineraryReqDTO
    ) {
        String userEmail = jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
        ItineraryRespDTO savedItinerary = itineraryUpdateService.createItinerary(userEmail, tripId, itineraryReqDTO);
        return ResponseEntity.ok(savedItinerary);
    }

    @PatchMapping("/{itineraryId}")
    public ResponseEntity<ItineraryRespDTO> updateItinerary(
            @PathVariable Long itineraryId,
            @RequestHeader("Authorization") String token,
            @RequestBody ItineraryReqDTO itineraryReqDTO
    ) {
        String userEmail = jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
        ItineraryRespDTO updatedItinerary = itineraryUpdateService.updateItinerary(userEmail, itineraryId, itineraryReqDTO);
        return ResponseEntity.ok(updatedItinerary);
    }

    @DeleteMapping("/{itineraryId}")
    public ResponseEntity<?> deleteItinerary(
            @PathVariable Long itineraryId,
            @RequestHeader("Authorization") String token
    ) {
        String userEmail = jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
        boolean isDeleted = itineraryUpdateService.deleteItinerary(userEmail, itineraryId);
        return isDeleted ? ResponseEntity.ok("일정이 삭제되었습니다.") : ResponseEntity.badRequest().build();
    }
}
