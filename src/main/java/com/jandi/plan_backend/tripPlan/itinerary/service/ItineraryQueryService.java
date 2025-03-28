package com.jandi.plan_backend.tripPlan.itinerary.service;

import com.jandi.plan_backend.tripPlan.itinerary.dto.ItineraryRespDTO;
import com.jandi.plan_backend.tripPlan.itinerary.entity.Itinerary;
import com.jandi.plan_backend.tripPlan.itinerary.repository.ItineraryRepository;
import com.jandi.plan_backend.tripPlan.place.dto.PlaceRespDTO;
import com.jandi.plan_backend.tripPlan.place.entity.Place;
import com.jandi.plan_backend.tripPlan.place.repository.PlaceRepository;
import com.jandi.plan_backend.tripPlan.trip.entity.Trip;
import com.jandi.plan_backend.tripPlan.trip.entity.TripParticipant;
import com.jandi.plan_backend.tripPlan.trip.repository.TripParticipantRepository;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.util.PlaceUtil;
import com.jandi.plan_backend.util.TripUtil;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItineraryQueryService {
    private final ValidationUtil validationUtil;
    private final TripUtil tripUtil;
    private final PlaceUtil placeUtil;
    private final ItineraryRepository itineraryRepository;
    private final UserRepository userRepository;

    public List<ItineraryRespDTO> getItineraries(String userEmail, Integer tripId) {
        Trip trip = validationUtil.validateTripExists(tripId);
        User user = userRepository.findByEmail(userEmail).orElse(null);

        // 접근 권한 검증
        boolean canViewTrip = tripUtil.isCanViewTrip(trip, user);
        if (!canViewTrip) {
            throw new BadRequestExceptionMessage("비공개 여행 계획입니다");
        }

        // 객체를 DTO로 변환하여 반환
        List<Itinerary> itineraries = itineraryRepository.findByTrip_TripId(tripId);
        return convertItinerariesToDto(itineraries);
    }

    private List<ItineraryRespDTO> convertItinerariesToDto(List<Itinerary> itineraries) {
        return itineraries.stream().map(itinerary -> {
            return placeUtil.convertPlaceToDto(itinerary.getPlaceId(), itinerary);
        }).collect(Collectors.toList());
    }
}
