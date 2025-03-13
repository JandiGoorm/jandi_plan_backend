package com.jandi.plan_backend.googlePlace.controller;

import com.jandi.plan_backend.googlePlace.dto.RecommPlaceReqDTO;
import com.jandi.plan_backend.googlePlace.dto.RecommPlaceRespDTO;
import com.jandi.plan_backend.googlePlace.service.RecommendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/map/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    /**
     * POST /api/map/recommend/restaurant
     * RequestBody로 { "cityId": 123 }를 받고,
     * DB에서 1개월 이내 데이터가 있으면 반환,
     * 없거나 오래됐으면 구글 API로 갱신 후 DB 저장 & 반환
     */
    @PostMapping("/restaurant")
    public ResponseEntity<List<RecommPlaceRespDTO>> getRecommendedPlaces(
            @Valid @RequestBody RecommPlaceReqDTO reqDTO
    ) {
        // cityId 유효성 검증(@Valid) → 실패 시 400 Bad Request 자동 응답
        List<RecommPlaceRespDTO> result = recommendService.getAllRecommendedPlace(reqDTO);
        return ResponseEntity.ok(result);
    }
}
