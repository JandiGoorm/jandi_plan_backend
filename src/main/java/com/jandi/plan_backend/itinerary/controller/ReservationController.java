package com.jandi.plan_backend.itinerary.controller;

import com.jandi.plan_backend.itinerary.dto.ReservationReqDTO;
import com.jandi.plan_backend.itinerary.dto.ReservationRespDTO;
import com.jandi.plan_backend.itinerary.entity.Reservation;
import com.jandi.plan_backend.itinerary.entity.ReservationCategory;
import com.jandi.plan_backend.itinerary.service.ReservationService;
import com.jandi.plan_backend.security.JwtTokenProvider;
import com.jandi.plan_backend.user.entity.Continent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
            @RequestParam String category,
            @RequestParam String title,
            @RequestParam Integer cost,
            @RequestParam(required = false) String description //숙박일 때만 필수이므로
    ) {
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        // DTO로 묶어 category를 한글 -> ENUM 형식으로 변환
        ReservationReqDTO reservedDTO = new ReservationReqDTO(category, title, description, cost);
        ReservationRespDTO savedReservation = reservationService.createReservation(userEmail, tripId, reservedDTO);
        return ResponseEntity.ok(savedReservation);
    }

    /** 예약 수정 */
    @PatchMapping("/{reservationId}")
    public ResponseEntity<?> updateReservation(
            @PathVariable Integer reservationId,
            @RequestHeader("Authorization") String token, // 헤더의 Authorization에서 JWT 토큰 받기
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer cost,
            @RequestParam(required = false) String description //숙박일 때만 필수이므로
    ) {
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        // DTO로 묶어 category를 한글 -> ENUM 형식으로 변환
        ReservationReqDTO reservedDTO = new ReservationReqDTO(category, title, description, cost);
        ReservationRespDTO savedReservation = reservationService.updateReservation(userEmail, reservationId, reservedDTO);
        return ResponseEntity.ok(savedReservation);
    }


}
