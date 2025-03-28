package com.jandi.plan_backend.util;

import com.jandi.plan_backend.tripPlan.itinerary.dto.ItineraryRespDTO;
import com.jandi.plan_backend.tripPlan.itinerary.entity.Itinerary;
import com.jandi.plan_backend.tripPlan.place.dto.PlaceRespDTO;
import com.jandi.plan_backend.tripPlan.place.entity.Place;
import com.jandi.plan_backend.tripPlan.place.repository.PlaceRepository;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PlaceUtil {
    private final PlaceRepository placeRepository;

    public ItineraryRespDTO convertPlaceToDto(Long placeId, Itinerary itinerary) {
        Optional<Place> placeOpt = placeRepository.findById(placeId);

        if(placeOpt.isPresent())
            return convertPlaceToDto(placeOpt.get(), itinerary);
        else
            throw new BadRequestExceptionMessage("일정에 연결된 장소 정보가 없습니다. placeId: " + placeId);
    }

    public ItineraryRespDTO convertPlaceToDto(Place place, Itinerary itinerary) {
        PlaceRespDTO placeDto = new PlaceRespDTO(
                place.getPlaceId(),
                place.getName(),
                place.getAddress(),
                place.getLatitude(),
                place.getLongitude()
        );
        return new ItineraryRespDTO(itinerary, placeDto);
    }
}
