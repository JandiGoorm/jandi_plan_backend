package com.jandi.plan_backend.tripPlan.reservation.controller;

import com.jandi.plan_backend.tripPlan.reservation.dto.ReservationReqDTO;
import com.jandi.plan_backend.tripPlan.reservation.dto.ReservationRespDTO;
import com.jandi.plan_backend.tripPlan.reservation.service.ReservationService;
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

    @GetMapping("/{tripId}")
    public ResponseEntity<?> getReservation(
            @PathVariable Integer tripId,
            @RequestHeader("Authorization") String token
    ) {
        String userEmail = jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
        Map<String, Object> reservations = reservationService.getReservation(userEmail, tripId);
        return ResponseEntity.ok(reservations);
    }

    @PostMapping("/{tripId}")
    public ResponseEntity<?> createReservation(
            @PathVariable Integer tripId,
            @RequestHeader("Authorization") String token,
            @RequestBody ReservationReqDTO reservedDTO
    ) {
        String userEmail = jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
        ReservationRespDTO savedReservation = reservationService.createReservation(userEmail, tripId, reservedDTO);
        return ResponseEntity.ok(savedReservation);
    }

    @PatchMapping("/{reservationId}")
    public ResponseEntity<?> updateReservation(
            @PathVariable Integer reservationId,
            @RequestHeader("Authorization") String token,
            @RequestBody ReservationReqDTO reservedDTO
    ) {
        String userEmail = jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
        ReservationRespDTO savedReservation = reservationService.updateReservation(userEmail, reservationId, reservedDTO);
        return ResponseEntity.ok(savedReservation);
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<?> deleteReservation(
            @PathVariable Integer reservationId,
            @RequestHeader("Authorization") String token
    ) {
        String userEmail = jwtTokenProvider.getEmail(token.replace("Bearer ", ""));
        boolean isSucceed = reservationService.deleteReservation(userEmail, reservationId);
        return isSucceed ? ResponseEntity.ok("삭제되었습니다") : ResponseEntity.badRequest().build();
    }
}
