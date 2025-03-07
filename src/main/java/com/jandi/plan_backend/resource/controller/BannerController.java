package com.jandi.plan_backend.resource.controller;

import com.jandi.plan_backend.resource.dto.*;
import com.jandi.plan_backend.resource.service.BannerService;
import com.jandi.plan_backend.security.JwtTokenProvider;
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

    public BannerController(BannerService bannerService, JwtTokenProvider jwtTokenProvider) {
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
    public ResponseEntity<?> writeBanner(
            @RequestHeader("Authorization") String token, // 헤더의 Authorization에서 JWT 토큰 받기
            @RequestPart MultipartFile file, //imageUrl에 넣을 원본 파일
            @RequestParam String title, //배너 제목
            @RequestParam String linkUrl //배너 클릭 시 연결할 link
    ) {
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        // 배너 저장 및 반환
        BannerRespDTO savedBanner = bannerService.writeBanner(userEmail, file, title, linkUrl);
        return ResponseEntity.ok(savedBanner);
    }

    /** 배너 수정 API */
    @PatchMapping("/lists/{bannerId}")
    public ResponseEntity<?> updateBanner(
            @PathVariable Integer bannerId,
            @RequestHeader("Authorization") String token, // 헤더의 Authorization에서 JWT 토큰 받기
            @RequestPart MultipartFile file, //imageUrl에 넣을 원본 파일
            @RequestParam String title, //배너 제목
            @RequestParam String linkUrl //배너 클릭 시 연결할 link
    ){
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        // 배너 수정 및 반환
        BannerRespDTO updatedBanner = bannerService.updateBanner(userEmail, bannerId, file, title, linkUrl);
        return ResponseEntity.ok(updatedBanner);
    }

    /** 배너 삭제 API */
    @DeleteMapping("/lists/{bannerId}")
    public ResponseEntity<?> deleteBanner(
            @PathVariable Integer bannerId, //삭제할 bannerId
            @RequestHeader("Authorization") String token // 헤더의 Authorization에서 JWT 토큰 받기
    ){
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        // 배너 삭제 및 반환
        String returnMsg = (bannerService.deleteBanner(userEmail, bannerId)) ?
                "삭제되었습니다" : "삭제 과정에서 문제가 발생했습니다. 다시 한번 시도해주세요";
        return ResponseEntity.ok(returnMsg);
    }
}
