package com.jandi.plan_backend.user.controller;

import com.jandi.plan_backend.storage.dto.ImageResponseDto;
import com.jandi.plan_backend.user.service.ProfileImageService;
import com.jandi.plan_backend.security.entity.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 프로필 사진 업로드 전용 컨트롤러.
 * 이 컨트롤러는 프로필 사진 업로드 요청을 처리하며,
 * 백엔드에서 인증된 사용자 정보를 기반으로 targetType을 "profile"로 고정하고
 * targetId를 인증된 사용자의 userId로 설정하여 이미지 업로드를 수행합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/profile-image")
public class ProfileImageController {

    private final ProfileImageService profileImageService;

    public ProfileImageController(ProfileImageService profileImageService) {
        this.profileImageService = profileImageService;
    }

    /**
     * 프로필 사진 업로드 API.
     *
     * @param file 업로드할 프로필 이미지 파일
     * @param customUserDetails 인증된 사용자 정보 (CustomUserDetails)
     * @return 업로드 결과를 담은 ImageResponseDto (이미지 ID, 파일명 등)
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        if (customUserDetails == null) {
            log.warn("인증되지 않은 사용자로부터 프로필 이미지 업로드 시도");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("프로필 이미지를 업로드하려면 로그인이 필요합니다.");
        }

        // 백엔드에서 targetType과 targetId를 재설정
        String ownerEmail = customUserDetails.getUsername();
        Integer userId = customUserDetails.getUserId(); // CustomUserDetails에 userId 제공 메서드가 있다고 가정

        log.info("사용자 '{}' (ID: {})가 프로필 이미지 업로드 요청", ownerEmail, userId);
        ImageResponseDto responseDto = profileImageService.uploadProfileImage(file, ownerEmail, userId);
        return ResponseEntity.ok(responseDto);
    }
}
