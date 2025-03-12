package com.jandi.plan_backend.googlePlace.controller;

import com.jandi.plan_backend.googlePlace.dto.RecommPlaceReqDTO;
import com.jandi.plan_backend.googlePlace.dto.RecommPlaceRespDTO;
import com.jandi.plan_backend.googlePlace.service.RecommendService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/map/recommend")
public class RecommendController {
    public final RecommendService recommendService;

    public RecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    // 맛집 조회
    @GetMapping("/restaurant")
    public ResponseEntity<?> getComments(
            @RequestBody RecommPlaceReqDTO reqDTO
    ){
        List<RecommPlaceRespDTO> recommendedPlace = recommendService.getAllRecommendedPlace(reqDTO);
        return ResponseEntity.ok(recommendedPlace);
    }
}
