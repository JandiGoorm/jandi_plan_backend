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
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("private") String isPrivate,
            @RequestParam("budget") Integer budget,
            @RequestParam("cityId") Integer cityId,
            @RequestParam("image") MultipartFile image
    ){
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        // 여행 계획 생성 및 반환
        TripRespDTO savedTrip = tripService.writeTrip(
                userEmail, title, startDate, endDate, isPrivate, budget, cityId, image);
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
     * @param title         수정할 여행 제목
     * @param isPrivate     "yes" 또는 "no"로 전달, "yes"면 비공개, "no"면 공개
     * @return 수정된 여행 계획 정보를 담은 TripRespDTO
     */
    @PatchMapping("/my/{tripId}")
    public ResponseEntity<?> updateMyTripBasicInfo(
            @RequestHeader("Authorization") String token,
            @PathVariable("tripId") Integer tripId,
            @RequestParam("title") String title,
            @RequestParam("private") String isPrivate
    ) {
        // JWT 토큰에서 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        // 여행 계획 수정
        TripRespDTO updatedTrip = tripService.updateTripBasicInfo(userEmail, tripId, title, isPrivate);
        return ResponseEntity.ok(updatedTrip);
    }
}
