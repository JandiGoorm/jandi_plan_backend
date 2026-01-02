package com.jandi.plan_backend.fixture;

import com.jandi.plan_backend.tripPlan.reservation.dto.ReservationReqDTO;
import com.jandi.plan_backend.tripPlan.reservation.entitiy.Reservation;
import com.jandi.plan_backend.tripPlan.reservation.entitiy.ReservationCategory;
import com.jandi.plan_backend.tripPlan.trip.entity.Trip;

import java.util.List;

/**
 * Reservation 관련 테스트 데이터 팩토리 클래스
 */
public class ReservationFixture {

    /**
     * 기본 예약 생성 (교통)
     */
    public static Reservation createReservation(Trip trip) {
        Reservation reservation = new Reservation();
        reservation.setReservationId(1L);
        reservation.setTrip(trip);
        reservation.setCategory(ReservationCategory.TRANSPORTATION);
        reservation.setTitle("KTX 서울-부산");
        reservation.setDescription("서울역 → 부산역 08:00");
        reservation.setCost(50000);
        return reservation;
    }

    /**
     * 특정 카테고리의 예약 생성
     */
    public static Reservation createReservationWithCategory(Trip trip, ReservationCategory category) {
        Reservation reservation = createReservation(trip);
        reservation.setCategory(category);
        reservation.setTitle(category.getDisplayName() + " 예약");
        return reservation;
    }

    /**
     * 특정 ID를 가진 예약 생성
     */
    public static Reservation createReservationWithId(Long reservationId, Trip trip) {
        Reservation reservation = createReservation(trip);
        reservation.setReservationId(reservationId);
        return reservation;
    }

    /**
     * 숙박 예약 생성
     */
    public static Reservation createAccommodationReservation(Trip trip) {
        Reservation reservation = new Reservation();
        reservation.setReservationId(2L);
        reservation.setTrip(trip);
        reservation.setCategory(ReservationCategory.ACCOMMODATION);
        reservation.setTitle("그랜드 호텔");
        reservation.setDescription("체크인: 15:00, 체크아웃: 11:00");
        reservation.setCost(150000);
        return reservation;
    }

    /**
     * 기타 예약 생성
     */
    public static Reservation createEtcReservation(Trip trip) {
        Reservation reservation = new Reservation();
        reservation.setReservationId(3L);
        reservation.setTrip(trip);
        reservation.setCategory(ReservationCategory.ETC);
        reservation.setTitle("관광 티켓");
        reservation.setDescription("디즈니랜드 1일권");
        reservation.setCost(100000);
        return reservation;
    }

    /**
     * 여러 카테고리의 예약 목록 생성
     */
    public static List<Reservation> createMixedReservations(Trip trip) {
        return List.of(
                createReservation(trip),
                createAccommodationReservation(trip),
                createEtcReservation(trip)
        );
    }

    /**
     * 예약 생성 요청 DTO
     */
    public static ReservationReqDTO createReservationReqDTO() {
        return new ReservationReqDTO("교통편", "KTX 서울-부산", "서울역 → 부산역 08:00", 50000);
    }

    /**
     * 예약 수정 요청 DTO
     */
    public static ReservationReqDTO createUpdateReservationReqDTO() {
        return new ReservationReqDTO("숙박", "신라 호텔", "체크인: 15:00", 200000);
    }

    /**
     * 특정 카테고리의 예약 요청 DTO
     */
    public static ReservationReqDTO createReservationReqDTO(String category, String title, String description, Integer cost) {
        return new ReservationReqDTO(category, title, description, cost);
    }
}
