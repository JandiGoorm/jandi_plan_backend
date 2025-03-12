package com.jandi.plan_backend.itinerary.controller;

import com.jandi.plan_backend.itinerary.dto.PlaceReqDTO;
import com.jandi.plan_backend.itinerary.dto.PlaceRespDTO;
import com.jandi.plan_backend.itinerary.service.PlaceService;
import com.jandi.plan_backend.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/place")
public class PlaceController {

    private final PlaceService placeService;
    private final JwtTokenProvider jwtTokenProvider;

    public PlaceController(PlaceService placeService, JwtTokenProvider jwtTokenProvider) {
        this.placeService = placeService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping
    public ResponseEntity<PlaceRespDTO> createPlace(
            @RequestHeader("Authorization") String token,
            @RequestBody PlaceReqDTO placeReqDTO
    ) {
        String userEmail = jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
        PlaceRespDTO savedPlace = placeService.createPlace(userEmail, placeReqDTO);
        return ResponseEntity.ok(savedPlace);
    }

    @GetMapping("/{placeId}")
    public ResponseEntity<PlaceRespDTO> getPlace(@PathVariable Long placeId) {
        PlaceRespDTO placeRespDTO = placeService.getPlace(placeId);
        return ResponseEntity.ok(placeRespDTO);
    }

    @GetMapping
    public ResponseEntity<List<PlaceRespDTO>> getAllPlaces() {
        List<PlaceRespDTO> places = placeService.getAllPlaces();
        return ResponseEntity.ok(places);
    }
}
