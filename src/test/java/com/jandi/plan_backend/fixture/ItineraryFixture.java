package com.jandi.plan_backend.fixture;

import com.jandi.plan_backend.tripPlan.itinerary.dto.ItineraryReqDTO;
import com.jandi.plan_backend.tripPlan.itinerary.entity.Itinerary;
import com.jandi.plan_backend.tripPlan.trip.entity.Trip;
import com.jandi.plan_backend.util.TimeUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Itinerary 관련 테스트 데이터 팩토리 클래스
 */
public class ItineraryFixture {

    private static final LocalDate NOW = TimeUtil.now().toLocalDate();

    /**
     * 기본 일정 생성
     */
    public static Itinerary createItinerary(Trip trip) {
        Itinerary itinerary = new Itinerary();
        itinerary.setItineraryId(1L);
        itinerary.setTrip(trip);
        itinerary.setPlaceId(12345L);
        itinerary.setTitle("테스트 일정");
        itinerary.setDate(NOW.plusDays(7));
        itinerary.setStartTime(LocalTime.of(10, 0));
        itinerary.setCost(10000);
        itinerary.setCreatedAt(NOW);
        return itinerary;
    }

    /**
     * 특정 ID를 가진 일정 생성
     */
    public static Itinerary createItineraryWithId(Long itineraryId, Trip trip) {
        Itinerary itinerary = createItinerary(trip);
        itinerary.setItineraryId(itineraryId);
        itinerary.setTitle("테스트 일정 " + itineraryId);
        return itinerary;
    }

    /**
     * 특정 날짜의 일정 생성
     */
    public static Itinerary createItineraryWithDate(Trip trip, LocalDate date) {
        Itinerary itinerary = createItinerary(trip);
        itinerary.setDate(date);
        return itinerary;
    }

    /**
     * 일정 생성 요청 DTO
     */
    public static ItineraryReqDTO createItineraryReqDTO() {
        return new ItineraryReqDTO(
                12345L,
                NOW.plusDays(7).toString(),
                "10:00",
                "새 일정",
                15000
        );
    }

    /**
     * 일정 수정 요청 DTO
     */
    public static ItineraryReqDTO createUpdateReqDTO() {
        return new ItineraryReqDTO(
                null, // placeId는 변경하지 않음
                null, // date도 변경하지 않음
                "14:00",
                "수정된 일정",
                20000
        );
    }

    /**
     * 여러 일정 목록 생성
     */
    public static List<Itinerary> createItineraryList(Trip trip, int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> createItineraryWithId((long) i, trip))
                .toList();
    }
}
