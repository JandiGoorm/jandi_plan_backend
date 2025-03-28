package com.jandi.plan_backend.tripPlan.itinerary.service;

import com.jandi.plan_backend.tripPlan.itinerary.dto.ItineraryReqDTO;
import com.jandi.plan_backend.tripPlan.itinerary.dto.ItineraryRespDTO;
import com.jandi.plan_backend.tripPlan.itinerary.entity.Itinerary;
import com.jandi.plan_backend.tripPlan.itinerary.repository.ItineraryRepository;
import com.jandi.plan_backend.tripPlan.place.dto.PlaceRespDTO;
import com.jandi.plan_backend.tripPlan.place.entity.Place;
import com.jandi.plan_backend.tripPlan.place.repository.PlaceRepository;
import com.jandi.plan_backend.tripPlan.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.PlaceUtil;
import com.jandi.plan_backend.util.TripUtil;
import com.jandi.plan_backend.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ItineraryUpdateService {
    private final ValidationUtil validationUtil;
    private final ItineraryRepository itineraryRepository;
    private final TripUtil tripUtil;
    private final PlaceUtil placeUtil;

    public ItineraryRespDTO createItinerary(String userEmail, Integer tripId, ItineraryReqDTO reqDTO) {
        User user = validationUtil.validateUserExists(userEmail);
        Trip trip = validationUtil.validateTripExists(tripId);
        tripUtil.isCanEditTrip(trip, user);

        // 일정 생성
        Itinerary itinerary = createItineraryData(trip, reqDTO);

        // 객체를 dto로 변환하여 반환
        return placeUtil.convertPlaceToDto(itinerary.getPlaceId(), itinerary);
    }

    public ItineraryRespDTO updateItinerary(String userEmail, Long itineraryId, ItineraryReqDTO reqDTO) {
        User user = validationUtil.validateUserExists(userEmail);
        Itinerary itinerary = validationUtil.validateItineraryExists(itineraryId);
        Trip trip = itinerary.getTrip();
        tripUtil.isCanEditTrip(trip, user);

        // 일정 수정
        updateItineraryData(itinerary, reqDTO);

        // 객체를 dto로 변환하여 반환
        return placeUtil.convertPlaceToDto(itinerary.getPlaceId(), itinerary);
    }

    public boolean deleteItinerary(String userEmail, Long itineraryId) {
        User user = validationUtil.validateUserExists(userEmail);
        Itinerary itinerary = validationUtil.validateItineraryExists(itineraryId);
        Trip trip = itinerary.getTrip();
        tripUtil.isCanEditTrip(trip, user);

        // 일정 삭제
        itineraryRepository.delete(itinerary);
        return !itineraryRepository.existsById(itineraryId);
    }

    private Itinerary createItineraryData(Trip trip, ItineraryReqDTO reqDTO) {
        LocalDate date = LocalDate.parse(reqDTO.getDate());
        LocalTime startTime = LocalTime.parse(reqDTO.getStartTime());

        Itinerary itinerary = new Itinerary();
        itinerary.setTrip(trip);
        itinerary.setCreatedAt(date); // 필요에 따라 변경
        itinerary.setPlaceId(reqDTO.getPlaceId());
        itinerary.setStartTime(startTime);
        itinerary.setTitle(reqDTO.getTitle());
        itinerary.setCost(reqDTO.getCost());
        itinerary.setDate(date);

        itineraryRepository.save(itinerary);
        return itinerary;
    }

    private void updateItineraryData(Itinerary itinerary, ItineraryReqDTO reqDTO) {
        if (reqDTO.getPlaceId() != null) { // 장소 변경
            itinerary.setPlaceId(reqDTO.getPlaceId());
        }
        if (reqDTO.getDate() != null) { // 날짜 변경
            itinerary.setDate(LocalDate.parse(reqDTO.getDate()));
        }
        if (reqDTO.getStartTime() != null) { // 시간 변경
            itinerary.setStartTime(LocalTime.parse(reqDTO.getStartTime()));
        }
        if (reqDTO.getTitle() != null) { // 제목 변경
            itinerary.setTitle(reqDTO.getTitle());
        }
        if (reqDTO.getCost() != null) { // 비용 변경
            itinerary.setCost(reqDTO.getCost());
        }
        itineraryRepository.save(itinerary);
    }

}
