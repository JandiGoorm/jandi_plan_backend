package com.jandi.plan_backend.tripPlan.itinerary.controller;

import com.jandi.plan_backend.tripPlan.itinerary.dto.ItineraryReqDTO;
import com.jandi.plan_backend.tripPlan.itinerary.dto.ItineraryRespDTO;
import com.jandi.plan_backend.tripPlan.itinerary.service.ItineraryService;
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

    @GetMapping("/{tripId}")
    public ResponseEntity<?> getItineraries(
            @PathVariable Integer tripId,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        String userEmail = null;
        if (token != null) {
            userEmail = jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
        }
        List<ItineraryRespDTO> itineraryList = itineraryService.getItineraries(userEmail, tripId);
        return ResponseEntity.ok(itineraryList);
    }

    @PostMapping("/{tripId}")
    public ResponseEntity<ItineraryRespDTO> createItinerary(
            @PathVariable Integer tripId,
            @RequestHeader("Authorization") String token,
            @RequestBody ItineraryReqDTO itineraryReqDTO
    ) {
        String userEmail = jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
        ItineraryRespDTO savedItinerary = itineraryService.createItinerary(userEmail, tripId, itineraryReqDTO);
        return ResponseEntity.ok(savedItinerary);
    }

    @PatchMapping("/{itineraryId}")
    public ResponseEntity<ItineraryRespDTO> updateItinerary(
            @PathVariable Long itineraryId,
            @RequestHeader("Authorization") String token,
            @RequestBody ItineraryReqDTO itineraryReqDTO
    ) {
        String userEmail = jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
        ItineraryRespDTO updatedItinerary = itineraryService.updateItinerary(userEmail, itineraryId, itineraryReqDTO);
        return ResponseEntity.ok(updatedItinerary);
    }

    @DeleteMapping("/{itineraryId}")
    public ResponseEntity<?> deleteItinerary(
            @PathVariable Long itineraryId,
            @RequestHeader("Authorization") String token
    ) {
        String userEmail = jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
        boolean isDeleted = itineraryService.deleteItinerary(userEmail, itineraryId);
        return isDeleted ? ResponseEntity.ok("일정이 삭제되었습니다.") : ResponseEntity.badRequest().build();
    }
}
