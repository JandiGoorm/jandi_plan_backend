package com.jandi.plan_backend.itinerary.controller;

import com.jandi.plan_backend.itinerary.dto.ReservationReqDTO;
import com.jandi.plan_backend.itinerary.dto.ReservationRespDTO;
import com.jandi.plan_backend.itinerary.service.ReservationService;
import com.jandi.plan_backend.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/trip/reservation")
public class ReservationController {
    private final ReservationService reservationService;
    private final JwtTokenProvider jwtTokenProvider;

    public ReservationController(ReservationService reservationService, JwtTokenProvider jwtTokenProvider) {
        this.reservationService = reservationService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /** 예약 조회 */
    @GetMapping("/{tripId}")
    public ResponseEntity<?> getReservation(
            @PathVariable Integer tripId,
            @RequestHeader("Authorization") String token // 헤더의 Authorization에서 JWT 토큰 받기
    ) {
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        Map<String, Object> reservations = reservationService.getReservation(userEmail, tripId);
        return ResponseEntity.ok(reservations);
    }

    /** 예약 추가 */
    @PostMapping("/{tripId}")
    public ResponseEntity<?> createReservation(
            @PathVariable Integer tripId,
            @RequestHeader("Authorization") String token, // 헤더의 Authorization에서 JWT 토큰 받기
            @RequestBody ReservationReqDTO reservedDTO
    ) {
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        ReservationRespDTO savedReservation = reservationService.createReservation(userEmail, tripId, reservedDTO);
        return ResponseEntity.ok(savedReservation);
    }

    /** 예약 수정 */
    @PatchMapping("/{reservationId}")
    public ResponseEntity<?> updateReservation(
            @PathVariable Integer reservationId,
            @RequestHeader("Authorization") String token, // 헤더의 Authorization에서 JWT 토큰 받기
            @RequestBody ReservationReqDTO reservedDTO
    ) {
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        ReservationRespDTO savedReservation = reservationService.updateReservation(userEmail, reservationId, reservedDTO);
        return ResponseEntity.ok(savedReservation);
    }

    /** 예약 삭제 */
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<?> deleteReservation(
            @PathVariable Integer reservationId,
            @RequestHeader("Authorization") String token // 헤더의 Authorization에서 JWT 토큰 받기
    ) {
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        boolean isSucceed = reservationService.deleteReservation(userEmail, reservationId);
        return (isSucceed) ?
                ResponseEntity.ok("삭제되었습니다") : ResponseEntity.badRequest().build();
    }
}
