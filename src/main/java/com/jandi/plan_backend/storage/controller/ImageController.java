package com.jandi.plan_backend.storage.controller;

import com.jandi.plan_backend.storage.dto.ImageResponseDto;
import com.jandi.plan_backend.storage.service.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 이미지 업로드 및 범용 CRUD API를 제공하는 컨트롤러.
 * - 업로드: POST /api/images/upload
 * - 조회: GET /api/images/{imageId}
 * - 수정(업데이트): PUT /api/images/{imageId}
 * - 삭제: DELETE /api/images/{imageId}
 */
@Slf4j
@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * 이미지 업로드 API.
     * 인증된 사용자의 토큰 정보를 이용하여 파일을 업로드한 후,
     * DB에 이미지 정보를 저장하고, 전체 공개 URL을 반환합니다.
     *
     * @param file 업로드할 이미지 파일
     * @param targetId 대상 엔티티의 식별자 (예: 사용자 ID, 게시글 ID 등)
     * @param targetType 이미지가 속하는 대상 (예: "user", "advertise", "notice", "community" 등)
     * @param userDetails 인증된 사용자 정보 (여기서 이메일을 추출)
     * @return 업로드된 이미지 정보와 메시지를 담은 ImageResponseDto
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
                    .body("이미지 업로드를 위해 로그인이 필요합니다.");
        }
        String email = userDetails.getUsername();
        log.info("사용자 '{}'가 파일 '{}' 업로드 요청 (targetType: {}, targetId: {})",
                email, file.getOriginalFilename(), targetType, targetId);

        ImageResponseDto responseDto = imageService.uploadImage(file, email, targetId, targetType);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * 이미지 조회 API.
     * 경로 변수로 전달받은 이미지 ID를 기반으로 DB에 저장된 파일명을 바탕으로 공개 URL을 반환합니다.
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
     * 이미지 수정(업데이트) API.
     * 경로 변수로 전달받은 이미지 ID를 기반으로 기존 이미지를 새 파일로 업데이트합니다.
     * 기존 파일은 클라우드 스토리지에서 삭제한 후, 새 파일을 업로드하고 DB 레코드를 갱신합니다.
     *
     * @param imageId 업데이트할 이미지의 DB ID (경로 변수)
     * @param file 새로 업로드할 이미지 파일
     * @param userDetails 인증된 사용자 정보 (필요시 소유권 확인에 사용)
     * @return 업데이트된 이미지 정보와 메시지를 담은 ImageResponseDto
     */
    @PutMapping("/{imageId}")
    public ResponseEntity<?> updateImage(
            @PathVariable("imageId") Integer imageId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            log.warn("인증되지 않은 사용자로부터 이미지 업데이트 시도");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("이미지 업데이트를 위해 로그인이 필요합니다.");
        }
        // (선택) 이미지 소유권 체크 로직 추가 가능
        ImageResponseDto updatedDto = imageService.updateImage(imageId, file);
        if (updatedDto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "업데이트할 이미지를 찾을 수 없습니다."));
        }
        log.info("이미지 업데이트 성공: 이미지 ID {}", imageId);
        return ResponseEntity.ok(updatedDto);
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
