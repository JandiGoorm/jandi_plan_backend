package com.jandi.plan_backend.itinerary.controller;

import com.jandi.plan_backend.itinerary.dto.PlaceReqDTO;
import com.jandi.plan_backend.itinerary.dto.PlaceRespDTO;
import com.jandi.plan_backend.itinerary.service.PlaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/place")
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    /** 장소 생성 */
    @PostMapping
    public ResponseEntity<PlaceRespDTO> createPlace(@RequestBody PlaceReqDTO placeReqDTO) {
        PlaceRespDTO savedPlace = placeService.createPlace(placeReqDTO);
        return ResponseEntity.ok(savedPlace);
    }

    /** 장소 단건 조회 */
    @GetMapping("/{placeId}")
    public ResponseEntity<PlaceRespDTO> getPlace(@PathVariable Long placeId) {
        PlaceRespDTO placeRespDTO = placeService.getPlace(placeId);
        return ResponseEntity.ok(placeRespDTO);
    }

    /** 전체 장소 조회 */
    @GetMapping
    public ResponseEntity<List<PlaceRespDTO>> getAllPlaces() {
        List<PlaceRespDTO> places = placeService.getAllPlaces();
        return ResponseEntity.ok(places);
    }

    /** 장소 수정 */
    @PutMapping("/{placeId}")
    public ResponseEntity<PlaceRespDTO> updatePlace(@PathVariable Long placeId,
                                                    @RequestBody PlaceReqDTO placeReqDTO) {
        PlaceRespDTO updatedPlace = placeService.updatePlace(placeId, placeReqDTO);
        return ResponseEntity.ok(updatedPlace);
    }

    /** 장소 삭제 */
    @DeleteMapping("/{placeId}")
    public ResponseEntity<?> deletePlace(@PathVariable Long placeId) {
        placeService.deletePlace(placeId);
        return ResponseEntity.ok("장소가 삭제되었습니다.");
    }
}
