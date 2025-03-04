package com.jandi.plan_backend.image.controller;

import com.jandi.plan_backend.image.dto.ImageRespDto;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.security.CustomUserDetails;
import com.jandi.plan_backend.trip.service.TripService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;

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
    private final TripService tripService;

    public PlanImageController(ImageService imageService, TripService tripService) {
        this.imageService = imageService;
        this.tripService = tripService;
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
     * 여행계획 이미지 업로드 API (수정/대체 방식)
     * targetType = "trip"
     * targetId = 이미 생성된 tripId (양수)
     *
     * 1) 로그인 사용자만 가능
     * 2) 본인 여행계획(trip)인지 확인
     * 3) 기존 이미지가 있으면 삭제
     * 4) 새 이미지 업로드
     */
    @PostMapping("/trip")
    public ResponseEntity<?> uploadTripImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("targetId") int tripId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        // 1) 인증 확인
        if (customUserDetails == null) {
            log.warn("인증되지 않은 사용자로부터 여행계획 이미지 업로드 시도");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "여행계획 이미지를 업로드하려면 로그인이 필요합니다."));
        }
        String ownerEmail = customUserDetails.getUsername();
        log.info("사용자 '{}'가 여행계획 이미지 업로드 요청 (tripId: {})", ownerEmail, tripId);

        // 2) 여행계획 작성자인지 검증
        boolean isOwner = tripService.isOwnerOfTrip(ownerEmail, tripId);
        if (!isOwner) {
            log.warn("사용자 '{}'가 자신이 소유하지 않은 tripId={}에 이미지를 업로드하려고 함", ownerEmail, tripId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "본인이 작성한 여행계획만 이미지 업로드 가능합니다."));
        }

        // 3) 기존 이미지 삭제 (이미 trip에 이미지가 1개 존재한다면 제거)
        imageService.getImageByTarget("trip", tripId).ifPresent(img -> {
            log.info("기존 여행계획 이미지(imageId={}) 삭제 후 새 이미지로 교체", img.getImageId());
            imageService.deleteImage(img.getImageId());
        });

        // 4) 새 이미지 업로드
        ImageRespDto responseDto = imageService.uploadImage(file, ownerEmail, tripId, "trip");
        log.info("새 여행계획 이미지 업로드 완료, imageId={}", responseDto.getImageId());

        // 5) 응답
        return ResponseEntity.ok(responseDto);
    }

}
