package com.jandi.plan_backend.itinerary.service;

import com.jandi.plan_backend.itinerary.entity.Place;
import com.jandi.plan_backend.itinerary.dto.PlaceReqDTO;
import com.jandi.plan_backend.itinerary.dto.PlaceRespDTO;
import com.jandi.plan_backend.itinerary.repository.PlaceRepository;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaceService {

    private final PlaceRepository placeRepository;

    public PlaceService(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    // Create
    public PlaceRespDTO createPlace(PlaceReqDTO placeReqDTO) {
        Place place = new Place();
        place.setName(placeReqDTO.getName());
        place.setAddress(placeReqDTO.getAddress());
        place.setLatitude(placeReqDTO.getLatitude());
        place.setLongitude(placeReqDTO.getLongitude());
        Place savedPlace = placeRepository.save(place);
        return mapToDTO(savedPlace);
    }

    // Read - 단건 조회
    public PlaceRespDTO getPlace(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 장소입니다."));
        return mapToDTO(place);
    }

    // Read - 전체 조회
    public List<PlaceRespDTO> getAllPlaces() {
        return placeRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Update
    public PlaceRespDTO updatePlace(Long placeId, PlaceReqDTO placeReqDTO) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 장소입니다."));
        // 필요한 필드만 업데이트 (모두 업데이트할 경우)
        place.setName(placeReqDTO.getName());
        place.setAddress(placeReqDTO.getAddress());
        place.setLatitude(placeReqDTO.getLatitude());
        place.setLongitude(placeReqDTO.getLongitude());
        Place updatedPlace = placeRepository.save(place);
        return mapToDTO(updatedPlace);
    }

    // Delete
    public void deletePlace(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 장소입니다."));
        placeRepository.delete(place);
    }

    // 엔티티를 DTO로 매핑하는 유틸 메서드
    private PlaceRespDTO mapToDTO(Place place) {
        return new PlaceRespDTO(place.getPlaceId(), place.getName(), place.getAddress(), place.getLatitude(), place.getLongitude());
    }
}
