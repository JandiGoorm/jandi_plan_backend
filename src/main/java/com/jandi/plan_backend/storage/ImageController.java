package com.jandi.plan_backend.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final GoogleCloudStorageService googleCloudStorageService;

    public ImageController(GoogleCloudStorageService googleCloudStorageService) {
        this.googleCloudStorageService = googleCloudStorageService;
    }

    /**
     * 이미지 업로드 API
     * - 로그인한 사용자만 접근 가능 (@AuthenticationPrincipal을 사용하여 현재 사용자 정보를 주입받음)
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            log.warn("Unauthorized upload attempt: No user details found.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("이미지 업로드를 위해서는 로그인이 필요합니다.");
        }
        log.info("User {} is uploading file: {}", userDetails.getUsername(), file.getOriginalFilename());
        try {
            String publicUrl = googleCloudStorageService.uploadFile(file);
            log.info("File uploaded successfully, URL: {}", publicUrl);
            return ResponseEntity.ok(publicUrl);
        } catch (IOException e) {
            log.error("File upload failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 업로드 실패: " + e.getMessage());
        }
    }

    /**
     * 이미지 다운로드 API
     * - 누구나 접근 가능 (인증 불필요)
     */
    @GetMapping("/download")
    public ResponseEntity<?> downloadImage(@RequestParam("fileName") String fileName) {
        log.info("Download request received for file: {}", fileName);
        byte[] content = googleCloudStorageService.downloadFile(fileName);
        if (content == null) {
            log.warn("File not found during download: {}", fileName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("파일을 찾을 수 없습니다.");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "image/jpeg"); // 실제 파일의 Content-Type에 맞춰 조정 가능
        log.info("File {} downloaded successfully", fileName);
        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }
}
