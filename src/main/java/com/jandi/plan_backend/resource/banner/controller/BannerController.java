package com.jandi.plan_backend.resource.banner.controller;

import com.jandi.plan_backend.resource.banner.dto.BannerListDTO;
import com.jandi.plan_backend.resource.banner.dto.BannerReqDTO;
import com.jandi.plan_backend.resource.banner.dto.BannerRespDTO;
import com.jandi.plan_backend.resource.banner.service.BannerQueryService;
import com.jandi.plan_backend.resource.banner.service.BannerUpdateService;
import com.jandi.plan_backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/banner")
public class BannerController {
    private final BannerUpdateService bannerUpdateService;
    private final BannerQueryService bannerQueryService;
    private final JwtTokenProvider jwtTokenProvider;

    /** 배너 목록 조회 API */
    @GetMapping("/lists")
    public Map<String, Object> getAllBannersFormatted() {
        List<BannerListDTO> banners = bannerQueryService.getAllBanners();
        return Map.of(
                "bannerInfo", Map.of("size", banners.size()),
                "items", banners
        );
    }

    /** 배너 작성 API */
    @PostMapping("/lists")
    public ResponseEntity<?> writeBanner(
            @RequestHeader("Authorization") String token,
            @RequestPart MultipartFile file,
            @RequestParam String title,
            @RequestParam(required = false) String subtitle,
            @RequestParam String linkUrl
    ) {
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        BannerReqDTO reqDTO = new BannerReqDTO(file, title, subtitle, linkUrl);
        BannerRespDTO savedBanner = bannerUpdateService.writeBanner(userEmail, reqDTO);
        return ResponseEntity.ok(savedBanner);
    }

    /** 배너 수정 API */
    @PatchMapping("/lists/{bannerId}")
    public ResponseEntity<?> updateBanner(
            @PathVariable Integer bannerId,
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "subtitle", required = false) String subtitle,
            @RequestParam(value = "linkUrl", required = false) String linkUrl
    ) {
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        BannerReqDTO reqDTO = new BannerReqDTO(file, title, subtitle, linkUrl);
        BannerRespDTO updatedBanner = bannerUpdateService.updateBanner(userEmail, bannerId, reqDTO);
        return ResponseEntity.ok(updatedBanner);
    }

    /** 배너 삭제 API */
    @DeleteMapping("/lists/{bannerId}")
    public ResponseEntity<?> deleteBanner(
            @PathVariable Integer bannerId,
            @RequestHeader("Authorization") String token
    ) {
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);
        try {
            boolean isDeleted = bannerUpdateService.deleteBanner(userEmail, bannerId);
            String returnMsg = isDeleted
                    ? "삭제되었습니다"
                    : "삭제 과정에서 문제가 발생했습니다. 다시 한번 시도해주세요";
            return ResponseEntity.ok(returnMsg);
        } catch (RuntimeException e) {
            // 서비스 계층에서 관리자 권한 검증 실패 시 RuntimeException 발생
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

}
