package com.jandi.plan_backend.itinerary.service;

import com.jandi.plan_backend.itinerary.entity.Place;
import com.jandi.plan_backend.itinerary.dto.PlaceReqDTO;
import com.jandi.plan_backend.itinerary.dto.PlaceRespDTO;
import com.jandi.plan_backend.itinerary.repository.PlaceRepository;
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

    /**
     * 장소 생성
     * 로그인한 사용자만 생성할 수 있도록 userEmail을 받아서 검증합니다.
     */
    public PlaceRespDTO createPlace(String userEmail, PlaceReqDTO reqDTO) {
        // userEmail을 통해 사용자 존재 여부를 검증(로그인 되어 있는지 확인)
        validationUtil.validateUserExists(userEmail);

        Place place = new Place();
        place.setName(reqDTO.getName());
        place.setAddress(reqDTO.getAddress());
        place.setLatitude(reqDTO.getLatitude());
        place.setLongitude(reqDTO.getLongitude());
        Place savedPlace = placeRepository.save(place);
        return mapToDTO(savedPlace);
    }

    /**
     * 장소 단건 조회
     */
    public PlaceRespDTO getPlace(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 장소입니다."));
        return mapToDTO(place);
    }

    /**
     * 전체 장소 조회
     */
    public List<PlaceRespDTO> getAllPlaces() {
        return placeRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private PlaceRespDTO mapToDTO(Place place) {
        return new PlaceRespDTO(place.getPlaceId(), place.getName(), place.getAddress(), place.getLatitude(), place.getLongitude());
    }
}
