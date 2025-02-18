package com.jandi.plan_backend.resource.controller;

import com.jandi.plan_backend.resource.dto.*;
import com.jandi.plan_backend.resource.service.BannerService;
import com.jandi.plan_backend.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/banner")
public class BannerController {
    private final BannerService bannerService;
    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 관련

    public BannerController(BannerService bannerService, JwtTokenProvider jwtTokenProvider) {
        this.bannerService = bannerService;
        this.jwtTokenProvider = jwtTokenProvider;
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

    @PostMapping("/lists")
    public ResponseEntity writeBanner(
            @RequestHeader("Authorization") String token, // 헤더의 Authorization에서 JWT 토큰 받기
            @RequestBody BannerWritePostDTO bannerDTO // JSON 형식으로 배너글 작성 정보 받기
    ) {
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        // 공지글 저장 및 반환
        BannerWriteRespDTO savedNotice = bannerService.writeBanner(bannerDTO, userEmail);
        return ResponseEntity.ok(savedNotice);
    }

}
