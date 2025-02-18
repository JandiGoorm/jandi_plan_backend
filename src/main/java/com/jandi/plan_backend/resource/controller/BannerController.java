package com.jandi.plan_backend.resource.controller;

import com.jandi.plan_backend.resource.dto.*;
import com.jandi.plan_backend.resource.service.BannerService;
import com.jandi.plan_backend.security.JwtTokenProvider;
import com.jandi.plan_backend.storage.service.ImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/banner")
public class BannerController {
    private final BannerService bannerService;
    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 관련

    public BannerController(BannerService bannerService, JwtTokenProvider jwtTokenProvider, ImageService imageService) {
        this.bannerService = bannerService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /** 배너 목록 조회 API */
    @GetMapping("/lists")
    public Map<String, Object> getAllBannersFormatted() {
        List<BannerListDTO> banners = bannerService.getAllBanners();

        return Map.of(
                "bannerInfo", Map.of("size", banners.size()), //배너 정보
                "items", banners //배너 데이터
        );
    }

    /** 배너 작성 API */
    @PostMapping("/lists")
    public ResponseEntity writeBanner(
            @RequestHeader("Authorization") String token, // 헤더의 Authorization에서 JWT 토큰 받기
            @RequestParam MultipartFile file, //imageUrl에 넣을 원본 파일
            @RequestParam String title, //배너 제목
            @RequestParam String linkUrl //배너 클릭 시 연결할 link
    ) {
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        // 배너 저장 및 반환
        BannerRespDTO savedNotice = bannerService.writeBanner(userEmail, file, title, linkUrl);
        return ResponseEntity.ok(savedNotice);
    }

}
