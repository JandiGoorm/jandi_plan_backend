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

    /**
     * 장소 생성 API
     * 로그인한 사용자만 장소를 생성할 수 있습니다.
     */
    @PostMapping
    public ResponseEntity<PlaceRespDTO> createPlace(
            @RequestHeader("Authorization") String token,
            @RequestBody PlaceReqDTO placeReqDTO
    ) {
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);
        PlaceRespDTO savedPlace = placeService.createPlace(userEmail, placeReqDTO);
        return ResponseEntity.ok(savedPlace);
    }

    /**
     * 장소 단건 조회 API
     * 누구나 조회 가능합니다.
     */
    @GetMapping("/{placeId}")
    public ResponseEntity<PlaceRespDTO> getPlace(@PathVariable Long placeId) {
        PlaceRespDTO placeRespDTO = placeService.getPlace(placeId);
        return ResponseEntity.ok(placeRespDTO);
    }

    /**
     * 전체 장소 조회 API
     * 누구나 조회 가능합니다.
     */
    @GetMapping
    public ResponseEntity<List<PlaceRespDTO>> getAllPlaces() {
        List<PlaceRespDTO> places = placeService.getAllPlaces();
        return ResponseEntity.ok(places);
    }
}
