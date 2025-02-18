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
 *
 * - 업로드: POST /api/images/profiles/upload
 *    -> 인증된 사용자의 토큰 정보를 이용해 프로필 사진을 업로드합니다.
 *
 * - 조회: GET /api/images/profiles/{userId}
 *    -> 경로 변수로 전달받은 사용자 ID를 기반으로 프로필 사진을 조회하여 공개 URL을 반환합니다.
 *
 * - 삭제: DELETE /api/images/profiles/delete
 *    -> 인증된 사용자의 토큰 정보를 이용해 프로필 사진을 삭제합니다.
 *
 * - 업데이트: PUT /api/images/profiles/update
 *    -> 인증된 사용자의 토큰 정보를 이용해 프로필 사진을 업데이트합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/images/profiles")
public class ProfileImageController {

    private final ProfileImageService profileImageService;

    public ProfileImageController(ProfileImageService profileImageService) {
        this.profileImageService = profileImageService;
    }

    /**
     * 프로필 사진 업로드 API.
     * 인증된 사용자의 토큰 정보를 이용하여 업로드를 처리하며,
     * targetType은 "profile"로 고정되고, targetId는 인증된 사용자의 userId로 설정됩니다.
     *
     * @param file 업로드할 프로필 이미지 파일
     * @param customUserDetails 인증된 사용자 정보 (CustomUserDetails)
     * @return 업로드 결과를 담은 ImageResponseDto (이미지 ID, 저장된 파일명, 메시지)
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
     * 경로 변수로 전달받은 사용자 ID를 기반으로 프로필 사진을 조회하여,
     * 저장된 파일명 앞에 공개 URL 접두어를 붙여 최종 URL을 반환합니다.
     *
     * @param userId 조회할 사용자 ID (경로 변수)
     * @return 프로필 이미지의 공개 URL을 담은 JSON 응답
     *         (예: {"imageUrl": "https://storage.googleapis.com/plan-storage/{파일명}"})
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getProfileImage(@PathVariable("userId") Integer userId) {
        Optional<com.jandi.plan_backend.storage.entity.Image> imageOpt = profileImageService.getProfileImage(userId);
        if (imageOpt.isPresent()) {
            String publicUrl = "https://storage.googleapis.com/plan-storage/" + imageOpt.get().getImageUrl();
            return ResponseEntity.ok(Map.of("imageUrl", publicUrl));
        } else {
            log.warn("프로필 이미지 조회 실패: 사용자 ID {}에 해당하는 이미지가 없음", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "프로필 이미지가 존재하지 않습니다."));
        }
    }

    /**
     * 프로필 사진 삭제 API.
     * 인증된 사용자의 토큰 정보를 이용하여 프로필 사진을 삭제합니다.
     *
     * @param customUserDetails 인증된 사용자 정보 (CustomUserDetails)
     * @return 삭제 결과 메시지를 담은 JSON 응답
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
     * 인증된 사용자의 토큰 정보를 이용하여 기존 프로필 사진을 삭제한 후,
     * 새 이미지를 업로드합니다.
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
