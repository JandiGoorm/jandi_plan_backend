package com.jandi.plan_backend.image.controller;

import com.jandi.plan_backend.image.dto.ImageRespDto;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 게시글 이미지 업로드 API를 제공하는 컨트롤러.
 * - 업로드: POST /api/images/upload/community
 *   인증된 사용자의 토큰 정보를 이용해 게시글 이미지를 업로드합니다.
 *   targetType은 "community"로 고정되며, targetId는 게시글의 ID로 설정됩니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/images/upload")
public class PlanImageController {

    private final ImageService imageService;

    public PlanImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * 게시글 이미지 업로드 API
     * targetId: 음수 int (임시 postId) or 양수 int (실제 postId)
     */
    @PostMapping("/community")
    public ResponseEntity<?> uploadPostImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("targetId") int targetId,  // 음수 int 가능
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        if (customUserDetails == null) {
            log.warn("인증되지 않은 사용자로부터 게시글 이미지 업로드 시도");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("게시글 이미지를 업로드하려면 로그인이 필요합니다.");
        }
        String ownerEmail = customUserDetails.getUsername();
        log.info("사용자 '{}'가 게시글 이미지 업로드 요청 (targetId: {})", ownerEmail, targetId);

        // targetType="community"
        ImageRespDto responseDto = imageService.uploadImage(file, ownerEmail, targetId, "community");
        return ResponseEntity.ok(responseDto);
    }

    /**
     * 공지사항 이미지 업로드 API.
     * targetType은 "notice"로 고정되며, targetId는 공지글의 ID(임시 또는 실제)입니다.
     */
    @PostMapping("/notice")
    public ResponseEntity<?> uploadNoticeImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("targetId") int targetId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        if (customUserDetails == null) {
            log.warn("인증되지 않은 사용자로부터 공지사항 이미지 업로드 시도");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("공지사항 이미지를 업로드하려면 로그인이 필요합니다.");
        }
        String ownerEmail = customUserDetails.getUsername();
        log.info("사용자 '{}'가 공지사항 이미지 업로드 요청 (targetId: {})", ownerEmail, targetId);

        // targetType="notice"
        ImageRespDto responseDto = imageService.uploadImage(file, ownerEmail, targetId, "notice");
        return ResponseEntity.ok(responseDto);
    }

    /**
     * 프로필 이미지 업로드 API.
     *
     * @param file 업로드할 프로필 이미지 파일
     * @param customUserDetails 인증된 사용자 정보 (CustomUserDetails)
     * @return 업로드 결과를 담은 ImageRespDto (이미지 ID, 전체 공개 URL, 메시지)
     */
    @PostMapping("/profile")
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
        // targetType을 "profile"로 고정하여 이미지 업로드 처리
        ImageRespDto responseDto = imageService.uploadImage(file, ownerEmail, userId, "profile");
        return ResponseEntity.ok(responseDto);
    }

    /**
     * 여행계획 이미지 업로드 API
     * targetType="trip"
     * targetId=양수 tripId (이미 생성된 여행 계획)
     */
    @PostMapping("/trip")
    public ResponseEntity<?> uploadTripImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("targetId") int tripId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        if (customUserDetails == null) {
            log.warn("인증되지 않은 사용자로부터 여행계획 이미지 업로드 시도");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("여행계획 이미지를 업로드하려면 로그인이 필요합니다.");
        }
        String ownerEmail = customUserDetails.getUsername();
        log.info("사용자 '{}'가 여행계획 이미지 업로드 요청 (tripId: {})", ownerEmail, tripId);

        // targetType="trip"으로 이미지 업로드
        ImageRespDto responseDto = imageService.uploadImage(file, ownerEmail, tripId, "trip");
        return ResponseEntity.ok(responseDto);
    }

}
