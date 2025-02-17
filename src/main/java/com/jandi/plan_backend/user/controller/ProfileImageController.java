package com.jandi.plan_backend.user.controller;

import com.jandi.plan_backend.storage.dto.ImageResponseDto;
import com.jandi.plan_backend.security.CustomUserDetails;
import com.jandi.plan_backend.user.service.ProfileImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

/**
 * 프로필 사진 관련 API를 제공하는 컨트롤러.
 * - 업로드: POST /api/profile-image/upload
 * - 조회:   GET /api/profile-image
 * - 삭제:   DELETE /api/profile-image/delete
 * - 업데이트: PUT /api/profile-image/update
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
     * @return 업로드 결과를 담은 ImageResponseDto
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
        String ownerEmail = customUserDetails.getUsername();
        Integer userId = customUserDetails.getUserId();

        log.info("사용자 '{}' (ID: {})가 프로필 이미지 업로드 요청", ownerEmail, userId);
        ImageResponseDto responseDto = profileImageService.uploadProfileImage(file, ownerEmail, userId);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * 프로필 사진 조회 API.
     * 인증된 사용자의 프로필 사진을 조회하여 공개 URL을 반환합니다.
     *
     * @param customUserDetails 인증된 사용자 정보 (CustomUserDetails)
     * @return 프로필 이미지의 공개 URL (JSON 형식)
     */
    @GetMapping("")
    public ResponseEntity<?> getProfileImage(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("프로필 이미지 조회를 위해 로그인이 필요합니다.");
        }
        Integer userId = customUserDetails.getUserId();
        Optional<com.jandi.plan_backend.storage.entity.Image> imageOpt = profileImageService.getProfileImage(userId);
        if (imageOpt.isPresent()) {
            String publicUrl = "https://storage.googleapis.com/plan-storage/" + imageOpt.get().getImageUrl();
            return ResponseEntity.ok(Map.of("imageUrl", publicUrl));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "프로필 이미지가 존재하지 않습니다."));
        }
    }

    /**
     * 프로필 사진 삭제 API.
     *
     * @param customUserDetails 인증된 사용자 정보 (CustomUserDetails)
     * @return 삭제 결과 메시지 (JSON 형식)
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteProfileImage(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("프로필 이미지 삭제를 위해 로그인이 필요합니다.");
        }
        Integer userId = customUserDetails.getUserId();
        boolean deleted = profileImageService.deleteProfileImage(userId);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "프로필 이미지가 삭제되었습니다."));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "삭제할 프로필 이미지가 존재하지 않습니다."));
        }
    }

    /**
     * 프로필 사진 업데이트 API.
     *
     * @param file 새로 업로드할 프로필 이미지 파일
     * @param customUserDetails 인증된 사용자 정보 (CustomUserDetails)
     * @return 업데이트 결과를 담은 ImageResponseDto
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateProfileImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("프로필 이미지 업데이트를 위해 로그인이 필요합니다.");
        }
        String ownerEmail = customUserDetails.getUsername();
        Integer userId = customUserDetails.getUserId();

        log.info("사용자 '{}' (ID: {})가 프로필 이미지 업데이트 요청", ownerEmail, userId);
        ImageResponseDto responseDto = profileImageService.updateProfileImage(file, ownerEmail, userId);
        return ResponseEntity.ok(responseDto);
    }
}
