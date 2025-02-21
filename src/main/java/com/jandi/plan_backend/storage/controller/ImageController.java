package com.jandi.plan_backend.storage.controller;

import com.jandi.plan_backend.storage.dto.ImageResponseDto;
import com.jandi.plan_backend.storage.service.ImageService;
import com.jandi.plan_backend.user.entity.MajorDestination;
import com.jandi.plan_backend.user.repository.MajorDestinationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

/**
 * 이미지 업로드와 공개 URL 조회를 담당하는 컨트롤러.
 * - 업로드 API: POST /api/images/upload
 *   -> 인증된 사용자의 토큰 정보를 이용해 파일 업로드 후 DB에 이미지 정보를 저장합니다.
 * - 공개 URL 조회 API: GET /api/images/{imageId}
 *   -> 경로 변수로 전달받은 이미지 ID를 기반으로 DB에 저장된 파일명을 바탕으로 공개 URL을 반환합니다.
 * - 삭제 API: DELETE /api/images/{imageId}
 *   -> 경로 변수로 전달받은 이미지 ID를 기반으로 클라우드 스토리지와 DB에서 이미지를 삭제합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;
    private final MajorDestinationRepository majorDestinationRepository;

    public ImageController(ImageService imageService, MajorDestinationRepository majorDestinationRepository) {
        this.imageService = imageService;
        this.majorDestinationRepository = majorDestinationRepository;
    }

    /**
     * 이미지 업로드 API.
     *
     * @param file 업로드할 이미지 파일
     * @param targetId 대상 엔티티의 식별자 (예: 사용자 ID, 게시글 ID 등)
     * @param targetType 이미지가 속하는 대상 (예: "user", "advertise", "notice", "community" 등)
     * @param userDetails 인증된 사용자 정보 (여기서 이메일을 추출)
     * @return 업로드된 이미지 정보와 메시지를 포함한 ResponseEntity
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("targetId") Integer targetId,
            @RequestParam("targetType") String targetType,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            log.warn("인증되지 않은 사용자로부터 업로드 시도: 사용자 정보 없음");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("이미지 업로드를 위해 로그인이 필요함");
        }
        // userDetails.getUsername()를 이메일로 사용
        String email = userDetails.getUsername();
        log.info("사용자 '{}'가 파일 '{}' 업로드 요청 (targetType: {}, targetId: {})",
                email, file.getOriginalFilename(), targetType, targetId);

        ImageResponseDto responseDto = imageService.uploadImage(file, email, targetId, targetType);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * 이미지 공개 URL 조회 API.
     * 경로 변수로 전달받은 이미지 ID를 기반으로, DB에 저장된 파일명을 바탕으로 공개 URL을 반환합니다.
     *
     * @param imageId 조회할 이미지의 ID (경로 변수)
     * @return {"imageUrl": "https://storage.googleapis.com/plan-storage/{파일명}"} 형태의 JSON 응답
     */
    @GetMapping("/{imageId}")
    public ResponseEntity<?> getPublicUrl(@PathVariable("imageId") Integer imageId) {
        String publicUrl = imageService.getPublicUrlByImageId(imageId);
        if (publicUrl == null) {
            log.warn("이미지 조회 실패: 이미지 ID {}에 해당하는 이미지가 없음", imageId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "이미지를 찾을 수 없습니다."));
        }
        log.info("이미지 조회 성공: 이미지 ID {}의 공개 URL: {}", imageId, publicUrl);
        return ResponseEntity.ok(Map.of("imageUrl", publicUrl));
    }

    /**
     * 이미지 삭제 API.
     * 경로 변수로 전달받은 이미지 ID를 기반으로 클라우드 스토리지와 DB에서 해당 이미지를 삭제합니다.
     *
     * @param imageId 삭제할 이미지의 DB ID (경로 변수)
     * @return 삭제 성공 시 JSON 응답, 실패 시 오류 메시지 반환
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<?> deleteImage(@PathVariable("imageId") Integer imageId) {
        boolean deleted = imageService.deleteImage(imageId);
        if (deleted) {
            log.info("이미지 삭제 성공: 이미지 ID {}", imageId);
            return ResponseEntity.ok(Map.of("message", "이미지가 성공적으로 삭제되었습니다."));
        } else {
            log.warn("이미지 삭제 실패: 이미지 ID {}에 해당하는 이미지가 없음", imageId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "삭제할 이미지가 존재하지 않습니다."));
        }
    }
}
