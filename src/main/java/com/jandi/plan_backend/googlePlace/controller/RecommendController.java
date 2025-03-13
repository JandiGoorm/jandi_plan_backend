package com.jandi.plan_backend.googlePlace.controller;

import com.jandi.plan_backend.googlePlace.dto.RecommPlaceReqDTO;
import com.jandi.plan_backend.googlePlace.dto.RecommPlaceRespDTO;
import com.jandi.plan_backend.googlePlace.service.RecommendService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 추천 장소(맛집 등) 관련 컨트롤러
 * - Google Places API를 호출하고, 그 결과를 DB에 저장한 뒤 반환합니다.
 */
@RestController
@RequestMapping("/api/map/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    public RecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    /**
     * 맛집 검색 API
     * - POST /api/map/recommend/restaurant
     * - Body(JSON): { "country": "일본", "city": "도쿄" }
     *
     * @param reqDTO 검색 조건 (국가, 도시)
     * @return 검색 결과(맛집 목록)
     */
    @PostMapping("/restaurant")
    public ResponseEntity<List<RecommPlaceRespDTO>> getRecommendedRestaurants(
            @RequestBody RecommPlaceReqDTO reqDTO
    ) {
        // Service에서 실제 Google Places API 호출 + DB 저장 로직을 처리
        List<RecommPlaceRespDTO> recommendedPlace = recommendService.getAllRecommendedPlace(reqDTO);
        return ResponseEntity.ok(recommendedPlace);
    }
}
