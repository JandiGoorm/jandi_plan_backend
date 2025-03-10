package com.jandi.plan_backend.trip.controller;

import com.jandi.plan_backend.security.JwtTokenProvider;
import com.jandi.plan_backend.trip.dto.*;
import com.jandi.plan_backend.trip.service.TripService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /** 전체 유저의 여행 계획 목록 조회 (공개 설정된 것만 조회) */
    @GetMapping("/allTrips")
    public Map<String, Object> getAllTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String token
    ){
        String userEmail = (token == null) ?
                null : jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
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
     * 좋아요 수 상위 10개 여행 계획 조회 API
     */
    @GetMapping("/top-likes")
    public ResponseEntity<List<TripRespDTO>> getTopLikes() {
        List<TripRespDTO> topTrips = tripService.getTop10Trips();
        return ResponseEntity.ok(topTrips);
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

    /** 좋아요한 여행 계획 목록 조회 */
    @GetMapping("/my/likedTrips")
    public Map<String, Object> getLikedTrips(
            @RequestHeader("Authorization") String token, // 헤더의 Authorization에서 JWT 토큰 받기
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

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

    /** 개별 여행 계획 조회
     * 공개로 설정된 다른 유저의 여행 계획 + 공개/비공개 설정된 본인의 여행 계획만 조회 가능
     */
    @GetMapping("/{tripId}")
    public ResponseEntity<?> getSpecTrips(
            @PathVariable Integer tripId,
            @RequestHeader(value = "Authorization", required = false) String token // ← required = false
    ){
        // userEmail 기본값은 null
        String userEmail = null;

        // token이 존재하고 비어있지 않다면, userEmail 추출
        if (token != null && !token.isBlank()) {
            String jwtToken = token.replace("Bearer ", "");
            userEmail = jwtTokenProvider.getEmail(jwtToken);
        }

        // Service 로직 호출
        TripItemRespDTO tripResp = tripService.getSpecTrips(userEmail, tripId);
        return ResponseEntity.ok(tripResp);
    }

    /** 여행 계획 생성 */
    @PostMapping("/my/create")
    public ResponseEntity<?> writeTrip(
            @RequestHeader("Authorization") String token,
            @RequestBody TripCreateReqDTO tripCreateReqDTO
    ){
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

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

    /** 타인의 여행 계획을 '좋아요' 목록에 추가 */
    @PostMapping("/my/likedTrips/{tripId}")
    public ResponseEntity<?> addLikeTrip(
            @PathVariable Integer tripId,
            @RequestHeader("Authorization") String token // 헤더의 Authorization에서 JWT 토큰 받기
    ){
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        TripLikeRespDTO likedTrip = tripService.addLikeTrip(userEmail, tripId);
        return ResponseEntity.ok(likedTrip);
    }

    /** 타인의 여행 계획을 '좋아요' 목록에서 삭제 */
    @DeleteMapping("/my/likedTrips/{tripId}")
    public ResponseEntity<?> deleteLikeTrip(
            @PathVariable Integer tripId,
            @RequestHeader("Authorization") String token // 헤더의 Authorization에서 JWT 토큰 받기
    ){
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        boolean isDeleteLikeTrip = tripService.deleteLikeTrip(userEmail, tripId);
        return (isDeleteLikeTrip) ?
                ResponseEntity.status(HttpStatus.OK).body("좋아요 해제되었습니다.") :
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("알 수 없는 문제가 발생했습니다. 잠시 후 다시 시도해주세요!");
    }

    /**
     * 내 여행 계획 삭제 API.
     * @param token   헤더에 담긴 JWT 토큰
     * @param tripId  삭제할 여행 계획의 고유 ID
     * @return 삭제 성공 메시지 또는 오류 메시지
     */
    @DeleteMapping("/my/{tripId}")
    public ResponseEntity<?> deleteMyTrip(
            @RequestHeader("Authorization") String token,
            @PathVariable("tripId") Integer tripId
    ) {
        // JWT 토큰에서 사용자 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        // 삭제 로직 수행
        tripService.deleteMyTrip(tripId, userEmail);

        // 성공적으로 삭제되었다면 메시지 반환
        return ResponseEntity.ok(Map.of("message", "해당 여행 계획이 삭제되었습니다."));
    }

    /**
     * 내 여행 계획 기본 정보 수정 (제목, 공개/비공개 여부 등)
     *
     * @param token         사용자 인증 토큰 (Authorization 헤더)
     * @param tripId        수정할 여행 계획 ID
     * @return 수정된 여행 계획 정보를 담은 TripRespDTO
     */
    @PatchMapping("/my/{tripId}")
    public ResponseEntity<?> updateMyTripBasicInfo(
            @RequestHeader("Authorization") String token,
            @PathVariable("tripId") Integer tripId,
            @RequestBody TripUpdateReqDTO tripUpdateReqDTO
    ) {
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        TripRespDTO updatedTrip = tripService.updateTripBasicInfo(
                userEmail,
                tripId,
                tripUpdateReqDTO.getTitle(),
                tripUpdateReqDTO.getPrivatePlan()
        );
        return ResponseEntity.ok(updatedTrip);
    }

    /**
     * 여행 계획 검색 API
     *
     * @param category 검색 카테고리 ("TITLE", "CITY", "BOTH")
     * @param keyword  검색어 (2글자 이상)
     * @param page     페이지 번호 (기본: 0)
     * @param size     페이지 크기 (기본: 10)
     * @return 검색 결과를 담은 페이지 객체 (TripRespDTO)
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchTrips(
            @RequestParam String category,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<TripRespDTO> tripsPage = tripService.searchTrips(category, keyword, page, size);
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
