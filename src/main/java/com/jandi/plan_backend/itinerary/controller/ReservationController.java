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

    /** 예약 추가 */
    @PostMapping("/{tripId}")
    public ResponseEntity<?> createReservation(
            @PathVariable Integer tripId,
            @RequestHeader("Authorization") String token, // 헤더의 Authorization에서 JWT 토큰 받기
            @RequestParam String category, String title, String description, Integer cost
    ) {
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        // ENUM 변환을 위해 DTO로 묶어 메서드에게 전달
        ReservationReqDTO reservedDTO = new ReservationReqDTO(category, title, description, cost);
        ReservationRespDTO savedReservation = reservationService.createReservation(userEmail, tripId, reservedDTO);
        return ResponseEntity.ok(savedReservation);
    }


}
