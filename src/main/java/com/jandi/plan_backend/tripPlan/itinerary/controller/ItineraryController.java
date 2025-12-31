package com.jandi.plan_backend.tripPlan.itinerary.controller;

import com.jandi.plan_backend.tripPlan.itinerary.dto.ItineraryReqDTO;
import com.jandi.plan_backend.tripPlan.itinerary.dto.ItineraryRespDTO;
import com.jandi.plan_backend.tripPlan.itinerary.service.ItineraryQueryService;
import com.jandi.plan_backend.tripPlan.itinerary.service.ItineraryUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trip/itinerary")
public class ItineraryController {
    private final ItineraryQueryService itineraryQueryService;
    private final ItineraryUpdateService itineraryUpdateService;

    @GetMapping("/{tripId}")
    public ResponseEntity<?> getItineraries(
            @PathVariable Integer tripId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = (userDetails != null) ? userDetails.getUsername() : null;
        List<ItineraryRespDTO> itineraryList = itineraryQueryService.getItineraries(userEmail, tripId);
        return ResponseEntity.ok(itineraryList);
    }

    @PostMapping("/{tripId}")
    public ResponseEntity<ItineraryRespDTO> createItinerary(
            @PathVariable Integer tripId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ItineraryReqDTO itineraryReqDTO
    ) {
        String userEmail = userDetails.getUsername();
        ItineraryRespDTO savedItinerary = itineraryUpdateService.createItinerary(userEmail, tripId, itineraryReqDTO);
        return ResponseEntity.ok(savedItinerary);
    }

    @PatchMapping("/{itineraryId}")
    public ResponseEntity<ItineraryRespDTO> updateItinerary(
            @PathVariable Long itineraryId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ItineraryReqDTO itineraryReqDTO
    ) {
        String userEmail = userDetails.getUsername();
        ItineraryRespDTO updatedItinerary = itineraryUpdateService.updateItinerary(userEmail, itineraryId, itineraryReqDTO);
        return ResponseEntity.ok(updatedItinerary);
    }

    @DeleteMapping("/{itineraryId}")
    public ResponseEntity<?> deleteItinerary(
            @PathVariable Long itineraryId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();
        boolean isDeleted = itineraryUpdateService.deleteItinerary(userEmail, itineraryId);
        return isDeleted ? ResponseEntity.ok("일정이 삭제되었습니다.") : ResponseEntity.badRequest().build();
    }
}
