package com.jandi.plan_backend.itinerary.service;

import com.jandi.plan_backend.itinerary.dto.ItineraryReqDTO;
import com.jandi.plan_backend.itinerary.dto.ItineraryRespDTO;
import com.jandi.plan_backend.itinerary.dto.PlaceRespDTO;
import com.jandi.plan_backend.itinerary.entity.Itinerary;
import com.jandi.plan_backend.itinerary.entity.Place;
import com.jandi.plan_backend.itinerary.repository.ItineraryRepository;
import com.jandi.plan_backend.itinerary.repository.PlaceRepository;
import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final ValidationUtil validationUtil;
    private final PlaceRepository placeRepository;

    public ItineraryService(ItineraryRepository itineraryRepository, ValidationUtil validationUtil, PlaceRepository placeRepository) {
        this.itineraryRepository = itineraryRepository;
        this.validationUtil = validationUtil;
        this.placeRepository = placeRepository;
    }

    public List<ItineraryRespDTO> getItineraries(String userEmail, Integer tripId) {
        Trip trip = validationUtil.validateTripExists(tripId);
        if (trip.getPrivatePlan()) {
            User user = validationUtil.validateUserExists(userEmail);
            if (!trip.getUser().getUserId().equals(user.getUserId())) {
                throw new BadRequestExceptionMessage("비공개 여행 계획은 작성자만 조회할 수 있습니다.");
            }
        }
        List<Itinerary> itineraries = itineraryRepository.findByTrip_TripId(tripId);

        return itineraries.stream().map(itinerary -> {
            Optional<Place> placeOpt = placeRepository.findById(itinerary.getPlaceId());
            if (placeOpt.isEmpty()) {
                throw new BadRequestExceptionMessage("일정에 연결된 장소 정보가 없습니다. placeId: " + itinerary.getPlaceId());
            }
            Place place = placeOpt.get();
            PlaceRespDTO placeDto = new PlaceRespDTO(
                    place.getPlaceId(),
                    place.getName(),
                    place.getAddress(),
                    place.getLatitude(),
                    place.getLongitude()
            );
            return new ItineraryRespDTO(itinerary, placeDto);
        }).collect(Collectors.toList());
    }

    public ItineraryRespDTO createItinerary(String userEmail, Integer tripId, ItineraryReqDTO reqDTO) {
        User user = validationUtil.validateUserExists(userEmail);
        Trip trip = validationUtil.validateTripExists(tripId);
        if (!trip.getUser().getUserId().equals(user.getUserId())) {
            throw new BadRequestExceptionMessage("본인이 작성한 여행 계획에만 일정을 추가할 수 있습니다.");
        }

        LocalDate date = LocalDate.parse(reqDTO.getDate());
        LocalTime startTime = LocalTime.parse(reqDTO.getStartTime());

        Itinerary itinerary = new Itinerary();
        itinerary.setTrip(trip);
        itinerary.setPlaceId(reqDTO.getPlaceId());
        itinerary.setDate(date);
        itinerary.setStartTime(startTime);
        itinerary.setTitle(reqDTO.getTitle());
        itinerary.setCost(reqDTO.getCost());
        itinerary.setCreatedAt(date); // 필요하다면 변경

        itineraryRepository.save(itinerary);

        Optional<Place> placeOpt = placeRepository.findById(itinerary.getPlaceId());
        if (placeOpt.isEmpty()) {
            throw new BadRequestExceptionMessage("일정에 연결된 장소 정보가 없습니다. placeId: " + itinerary.getPlaceId());
        }
        Place place = placeOpt.get();
        PlaceRespDTO placeDto = new PlaceRespDTO(
                place.getPlaceId(),
                place.getName(),
                place.getAddress(),
                place.getLatitude(),
                place.getLongitude()
        );
        return new ItineraryRespDTO(itinerary, placeDto);
    }

    public ItineraryRespDTO updateItinerary(String userEmail, Long itineraryId, ItineraryReqDTO reqDTO) {
        User user = validationUtil.validateUserExists(userEmail);
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 일정입니다."));

        if (!itinerary.getTrip().getUser().getUserId().equals(user.getUserId())) {
            throw new BadRequestExceptionMessage("본인이 작성한 여행 계획의 일정만 수정할 수 있습니다.");
        }

        if (reqDTO.getPlaceId() != null) {
            itinerary.setPlaceId(reqDTO.getPlaceId());
        }
        if (reqDTO.getDate() != null) {
            itinerary.setDate(LocalDate.parse(reqDTO.getDate()));
        }
        if (reqDTO.getStartTime() != null) {
            itinerary.setStartTime(LocalTime.parse(reqDTO.getStartTime()));
        }
        if (reqDTO.getTitle() != null) {
            itinerary.setTitle(reqDTO.getTitle());
        }
        if (reqDTO.getCost() != null) {
            itinerary.setCost(reqDTO.getCost());
        }
        itineraryRepository.save(itinerary);

        Optional<Place> placeOpt = placeRepository.findById(itinerary.getPlaceId());
        if (placeOpt.isEmpty()) {
            throw new BadRequestExceptionMessage("일정에 연결된 장소 정보가 없습니다. placeId: " + itinerary.getPlaceId());
        }
        Place place = placeOpt.get();
        PlaceRespDTO placeDto = new PlaceRespDTO(
                place.getPlaceId(),
                place.getName(),
                place.getAddress(),
                place.getLatitude(),
                place.getLongitude()
        );
        return new ItineraryRespDTO(itinerary, placeDto);
    }

    public boolean deleteItinerary(String userEmail, Long itineraryId) {
        User user = validationUtil.validateUserExists(userEmail);
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 일정입니다."));

        if (!itinerary.getTrip().getUser().getUserId().equals(user.getUserId())) {
            throw new BadRequestExceptionMessage("본인이 작성한 여행 계획의 일정만 삭제할 수 있습니다.");
        }
        itineraryRepository.delete(itinerary);
        return true;
    }
}
