package com.jandi.plan_backend.tripPlan.place.service;

import com.jandi.plan_backend.tripPlan.place.entity.Place;
import com.jandi.plan_backend.tripPlan.place.dto.PlaceReqDTO;
import com.jandi.plan_backend.tripPlan.place.dto.PlaceRespDTO;
import com.jandi.plan_backend.tripPlan.place.repository.PlaceRepository;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final ValidationUtil validationUtil;

    public PlaceService(PlaceRepository placeRepository, ValidationUtil validationUtil) {
        this.placeRepository = placeRepository;
        this.validationUtil = validationUtil;
    }

    public PlaceRespDTO createPlace(String userEmail, PlaceReqDTO reqDTO) {
        validationUtil.validateUserExists(userEmail);

        Place place = new Place();
        place.setName(reqDTO.getName());
        place.setAddress(reqDTO.getAddress());
        place.setLatitude(reqDTO.getLatitude());
        place.setLongitude(reqDTO.getLongitude());
        Place savedPlace = placeRepository.save(place);
        return mapToDTO(savedPlace);
    }

    public PlaceRespDTO getPlace(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 장소입니다."));
        return mapToDTO(place);
    }

    public List<PlaceRespDTO> getAllPlaces() {
        return placeRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private PlaceRespDTO mapToDTO(Place place) {
        return new PlaceRespDTO(
                place.getPlaceId(),
                place.getName(),
                place.getAddress(),
                place.getLatitude(),
                place.getLongitude()
        );
    }
}
