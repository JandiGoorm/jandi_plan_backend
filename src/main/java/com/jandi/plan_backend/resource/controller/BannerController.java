package com.jandi.plan_backend.resource.controller;

import com.jandi.plan_backend.resource.dto.BannerListDTO;
import com.jandi.plan_backend.resource.service.BannerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/banner")
public class BannerController {
    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    /**배너 목록 조회*/
    @GetMapping("/lists")
    public Map<String, Object> getAllBannersFormatted() {
        List<BannerListDTO> banners = bannerService.getAllBanners();

        return Map.of(
                "bannerInfo", Map.of("size", banners.size()), //배너 정보
                "items", banners //배너 데이터
        );
    }

}
