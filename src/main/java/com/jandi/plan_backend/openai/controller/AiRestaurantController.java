package com.jandi.plan_backend.openai.controller;

import com.jandi.plan_backend.openai.dto.AiRestaurantDTO;
import com.jandi.plan_backend.openai.service.AiRestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class AiRestaurantController {

    private final AiRestaurantService aiRestaurantService;

    public AiRestaurantController(AiRestaurantService aiRestaurantService) {
        this.aiRestaurantService = aiRestaurantService;
    }

    /**
     * 도시별 맛집 목록 조회 API
     * 예: GET /api/restaurants/city/35
     */
    @GetMapping("/city/{cityId}")
    public List<AiRestaurantDTO> getRestaurantsByCity(@PathVariable Integer cityId) {
        // Service에서 DTO 리스트를 반환
        return aiRestaurantService.getRestaurantsByCityDTO(cityId);
    }

    @PostMapping("/admin/update-all-restaurants")
    public ResponseEntity<?> updateAll() {
        aiRestaurantService.updateAllCitiesRestaurants();
        return ResponseEntity.ok("모든 도시의 맛집 정보를 업데이트했습니다.");
    }

}
