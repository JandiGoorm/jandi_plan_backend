package com.jandi.plan_backend.tripPlan.trip.controller;

import com.jandi.plan_backend.security.JwtTokenProvider;
import com.jandi.plan_backend.tripPlan.trip.dto.*;
import com.jandi.plan_backend.tripPlan.trip.service.TripService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 여행 계획 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/api/trip/")
public class TripController {

    private final TripService tripService;
    private final JwtTokenProvider jwtTokenProvider;

    public TripController(TripService tripService, JwtTokenProvider jwtTokenProvider) {
        this.tripService = tripService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 공개 설정된 여행 계획 목록 조회 (로그인 시 본인+타인 공개, 관리자면 전체)
     */
    @GetMapping("/allTrips")
    public Map<String, Object> getAllTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        String userEmail = (token == null)
                ? null
                : jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
        Page<TripRespDTO> tripsPage = tripService.getAllTrips(userEmail, page, size);

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

    /**
     * 좋아요 수가 많은 상위 10개 여행 계획 조회
     */
    @GetMapping("/top-likes")
    public ResponseEntity<List<TripRespDTO>> getTopLikes() {
        List<TripRespDTO> topTrips = tripService.getTop10Trips();
        return ResponseEntity.ok(topTrips);
    }

    /**
     * 내 여행 계획 목록 조회
     */
    @GetMapping("/my/allTrips")
    public Map<String, Object> getAllMyTrips(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String userEmail = jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
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

    /**
     * 좋아요한 여행 계획 목록 조회
     */
    @GetMapping("/my/likedTrips")
    public Map<String, Object> getLikedTrips(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String userEmail = jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
        Page<TripRespDTO> likedTripsPage = tripService.getLikedTrips(userEmail, page, size);

        return Map.of(
                "pageInfo", Map.of(
                        "currentPage", likedTripsPage.getNumber(),
                        "currentSize", likedTripsPage.getContent().size(),
                        "totalPages", likedTripsPage.getTotalPages(),
                        "totalSize", likedTripsPage.getTotalElements()
                ),
                "items", likedTripsPage.getContent()
        );
    }

    /**
     * 특정 여행 계획 단일 조회
     */
    @GetMapping("/{tripId}")
    public ResponseEntity<?> getSpecTrips(
            @PathVariable Integer tripId,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        String userEmail = null;
        if (token != null && !token.isBlank()) {
            userEmail = jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
        }
        TripItemRespDTO tripResp = tripService.getSpecTrips(userEmail, tripId);
        return ResponseEntity.ok(tripResp);
    }

    /**
     * 여행 계획 생성
     */
    @PostMapping("/my/create")
    public ResponseEntity<?> writeTrip(
            @RequestHeader("Authorization") String token,
            @RequestBody TripCreateReqDTO tripCreateReqDTO
    ) {
        String userEmail = jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
        TripRespDTO savedTrip = tripService.writeTrip(
                userEmail,
                tripCreateReqDTO.getTitle(),
                tripCreateReqDTO.getStartDate(),
                tripCreateReqDTO.getEndDate(),
                tripCreateReqDTO.getPrivatePlan(),
                tripCreateReqDTO.getBudget(),
                tripCreateReqDTO.getCityId()
        );
        return ResponseEntity.ok(savedTrip);
    }

    /**
     * 좋아요 추가
     */
    @PostMapping("/my/likedTrips/{tripId}")
    public ResponseEntity<?> addLikeTrip(
            @PathVariable Integer tripId,
            @RequestHeader("Authorization") String token
    ) {
        String userEmail = jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
        TripLikeRespDTO likedTrip = tripService.addLikeTrip(userEmail, tripId);
        return ResponseEntity.ok(likedTrip);
    }

    /**
     * 좋아요 해제
     */
    @DeleteMapping("/my/likedTrips/{tripId}")
    public ResponseEntity<?> deleteLikeTrip(
            @PathVariable Integer tripId,
            @RequestHeader("Authorization") String token
    ) {
        String userEmail = jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
        boolean isDeleteLikeTrip = tripService.deleteLikeTrip(userEmail, tripId);
        return (isDeleteLikeTrip)
                ? ResponseEntity.ok("좋아요 해제되었습니다.")
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("알 수 없는 문제가 발생했습니다.");
    }

    /**
     * 내 여행 계획 삭제
     */
    @DeleteMapping("/my/{tripId}")
    public ResponseEntity<?> deleteMyTrip(
            @RequestHeader("Authorization") String token,
            @PathVariable("tripId") Integer tripId
    ) {
        String userEmail = jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
        tripService.deleteMyTrip(tripId, userEmail);
        return ResponseEntity.ok(Map.of("message", "해당 여행 계획이 삭제되었습니다."));
    }

    /**
     * 내 여행 계획 기본 정보 수정 (제목, 공개/비공개)
     */
    @PatchMapping("/my/{tripId}")
    public ResponseEntity<?> updateMyTripBasicInfo(
            @RequestHeader("Authorization") String token,
            @PathVariable("tripId") Integer tripId,
            @RequestBody TripUpdateReqDTO tripUpdateReqDTO
    ) {
        String userEmail = jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
        TripRespDTO updatedTrip = tripService.updateTripBasicInfo(
                userEmail,
                tripId,
                tripUpdateReqDTO.getTitle(),
                tripUpdateReqDTO.getPrivatePlan()
        );
        return ResponseEntity.ok(updatedTrip);
    }

    /**
     * 여행 계획 검색
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchTrips(
            @RequestParam String category,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        // (1) userEmail 추출 (토큰이 없거나 비어 있으면 null)
        String userEmail = null;
        if (token != null && !token.isBlank()) {
            userEmail = jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
        }

        // (2) 검색
        Page<TripRespDTO> tripsPage = tripService.searchTrips(category, keyword, page, size, userEmail);

        // (3) 응답
        return ResponseEntity.ok(Map.of(
                "pageInfo", Map.of(
                        "currentPage", tripsPage.getNumber(),
                        "currentSize", tripsPage.getContent().size(),
                        "totalPages", tripsPage.getTotalPages(),
                        "totalSize", tripsPage.getTotalElements()
                ),
                "items", tripsPage.getContent()
        ));
    }
}
