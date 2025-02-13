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

/**
 * 이미지 업로드와 다운로드를 담당하는 컨트롤러.
 * 업로드 API는 로그인한 사용자만 접근할 수 있고, 다운로드 API는 누구나 접근할 수 있음.
 */
@Slf4j
@RestController
@RequestMapping("/api/images")
public class ImageController {

    // Google Cloud Storage에 파일 처리를 위임하는 서비스
    private final GoogleCloudStorageService googleCloudStorageService;

    // 생성자를 통해 GoogleCloudStorageService를 주입받음
    public ImageController(GoogleCloudStorageService googleCloudStorageService) {
        this.googleCloudStorageService = googleCloudStorageService;
    }

    /**
     * 이미지 업로드 API.
     * 로그인한 사용자만 접근 가능.
     * 요청 파라미터로 MultipartFile을 받고, 현재 인증된 사용자 정보를 받아서 업로드 처리.
     *
     * @param file 업로드할 이미지 파일
     * @param userDetails 인증된 사용자 정보 (null이면 인증되지 않은 상태)
     * @return 업로드된 이미지의 공개 URL을 포함한 ResponseEntity
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 사용자 정보가 없으면 인증되지 않은 상태이므로 401 응답 반환
        if (userDetails == null) {
            log.warn("인증되지 않은 사용자로부터 업로드 시도: 사용자 정보 없음");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("이미지 업로드를 위해 로그인이 필요함");
        }
        log.info("사용자 '{}'가 파일 '{}' 업로드 요청", userDetails.getUsername(), file.getOriginalFilename());

        try {
            // 서비스 클래스로 파일 업로드 처리 후 공개 URL 받아옴
            String publicUrl = googleCloudStorageService.uploadFile(file);
            log.info("파일 업로드 성공, 공개 URL: {}", publicUrl);
            return ResponseEntity.ok(publicUrl);
        } catch (IOException e) {
            // 파일 업로드 중 에러 발생 시 500 에러 응답 반환
            log.error("파일 업로드 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 업로드 실패: " + e.getMessage());
        }
    }

    /**
     * 이미지 다운로드 API.
     * 누구나 접근 가능.
     * 요청 파라미터로 파일명을 받아 해당 파일을 다운로드 후, 바이트 배열로 반환함.
     *
     * @param fileName 다운로드할 파일의 이름
     * @return 파일 데이터를 담은 ResponseEntity (헤더에 Content-Type 포함)
     */
    @GetMapping("/download")
    public ResponseEntity<?> downloadImage(@RequestParam("fileName") String fileName) {
        log.info("파일 다운로드 요청, 파일명: {}", fileName);
        byte[] content = googleCloudStorageService.downloadFile(fileName);

        // 파일을 찾지 못하면 404 응답 반환
        if (content == null) {
            log.warn("다운로드 실패: 파일을 찾지 못함 - 파일명: {}", fileName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("파일을 찾을 수 없음");
        }

        // 다운로드 성공 시, HTTP 헤더에 Content-Type 설정 후 200 응답 반환
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "image/jpeg"); // 필요에 따라 실제 콘텐츠 타입으로 수정
        log.info("파일 다운로드 성공: {}", fileName);
        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }
}
