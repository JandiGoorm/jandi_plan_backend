package com.jandi.plan_backend.googlePlace.controller;

import com.jandi.plan_backend.googlePlace.dto.RecommPlaceReqDTO;
import com.jandi.plan_backend.googlePlace.dto.RecommPlaceRespDTO;
import com.jandi.plan_backend.googlePlace.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/map/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    @PostMapping("/restaurant")
    public ResponseEntity<List<RecommPlaceRespDTO>> getRecommendedPlaces(@RequestBody RecommPlaceReqDTO reqDTO) {
        List<RecommPlaceRespDTO> result = recommendService.getAllRecommendedPlace(reqDTO);
        return ResponseEntity.ok(result);
    }
}
