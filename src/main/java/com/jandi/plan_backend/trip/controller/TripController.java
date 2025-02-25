package com.jandi.plan_backend.trip.controller;

import com.jandi.plan_backend.security.JwtTokenProvider;
import com.jandi.plan_backend.trip.dto.MyTripRespDTO;
import com.jandi.plan_backend.trip.dto.TripLikeRespDTO;
import com.jandi.plan_backend.trip.dto.TripRespDTO;
import com.jandi.plan_backend.trip.service.TripService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    /** 전체 유저의 여행 계획 목록 조회 (공개 설정된 것만 조회) */
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

    /** 내 여행 계획 목록 조회 (본인 명의의 계획만 조회) */
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

    /** 개별 여행 계획 조회
     * 공개로 설정된 다른 유저의 여행 계획 + 공개/비공개 설정된 본인의 여행 계획만 조회 가능
     * 아직 친구 기능은 구현되지 않아 이 부분은 아직 제외한 채로 구현
     */
    @GetMapping("/{tripId}")
    public ResponseEntity<?> getSpecTrips(
            @PathVariable Integer tripId,
            @RequestHeader(value = "Authorization", required = false) String token // 헤더의 Authorization에서 JWT 토큰 받기
    ){
        if(token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        return ResponseEntity.status(HttpStatus.OK)
                .body(tripService.getSpecTrips(userEmail, tripId));
    }

    /** 여행 계획 생성 */
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

    /** 타인의 여행 계획을 '좋아요' 목록에 추가 */
    @PostMapping("/like/{tripId}")
    public ResponseEntity<?> likeTrip(
            @PathVariable Integer tripId,
            @RequestHeader("Authorization") String token // 헤더의 Authorization에서 JWT 토큰 받기
    ){
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        TripLikeRespDTO likedTrip = tripService.addLikeTrip(userEmail, tripId);
        return ResponseEntity.ok(likedTrip);
    }

}
