package com.jandi.plan_backend.trip.controller;

import com.jandi.plan_backend.security.JwtTokenProvider;
import com.jandi.plan_backend.trip.dto.MyTripRespDTO;
import com.jandi.plan_backend.trip.dto.TripRespDTO;
import com.jandi.plan_backend.trip.service.TripService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trip/")
public class TripController {
    private final TripService tripService;
    public final JwtTokenProvider jwtTokenProvider;

    public TripController(TripService tripService, JwtTokenProvider jwtTokenProvider) {
        this.tripService = tripService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping("/allTrips")
    public Map<String, Object> getAllTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Page<TripRespDTO> tripsPage = tripService.getAllTrips(page, size);

        return Map.of(
                "pageInfo", Map.of(
                        "currentPage", tripsPage.getNumber(),
                        "currentSize", tripsPage.getContent().size(),
                        "totalPages", tripsPage.getTotalPages(),
                        "totalSize", tripsPage.getTotalElements()
                ),
                "items", tripsPage.getContent()
        );
    }

    @GetMapping("/my/allTrips")
    public Map<String, Object> getAllMyTrips(
            @RequestHeader("Authorization") String token, // 헤더의 Authorization에서 JWT 토큰 받기
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        Page<MyTripRespDTO> myTripsPage = tripService.getAllMyTrips(userEmail, page, size);

        return Map.of(
                "pageInfo", Map.of(
                        "currentPage", myTripsPage.getNumber(),
                        "currentSize", myTripsPage.getContent().size(),
                        "totalPages", myTripsPage.getTotalPages(),
                        "totalSize", myTripsPage.getTotalElements()
                ),
                "items", myTripsPage.getContent()
        );
    }

    @PostMapping("/my/create")
    public ResponseEntity<?> writeTrip(
            @RequestHeader("Authorization") String token, // 헤더의 Authorization에서 JWT 토큰 받기
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("private") String isPrivate,
            @RequestParam("image") MultipartFile image
    ){
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        // 여행 계획 생성 및 반환
        TripRespDTO savedTrip = tripService.writeTrip(
                userEmail, title, description, startDate, endDate, isPrivate, image);
        return ResponseEntity.ok(savedTrip);
    }

    /**
     * 좋아요 수 상위 10개 여행 계획 조회 API
     */
    @GetMapping("/top-likes")
    public ResponseEntity<List<TripRespDTO>> getTopLikes() {
        List<TripRespDTO> topTrips = tripService.getTop10Trips();
        return ResponseEntity.ok(topTrips);
    }
}
