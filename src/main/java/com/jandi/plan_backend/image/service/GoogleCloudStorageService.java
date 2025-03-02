package com.jandi.plan_backend.image.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Google Cloud Storage와의 연동을 담당하는 서비스.
 */
@Slf4j
@Service
public class GoogleCloudStorageService {

    private final Storage storage;

    @Value("${gcs.bucket.name:plan-storage}")
    private String bucketName;

    public GoogleCloudStorageService(Storage storage) {
        this.storage = storage;
    }

    /**
     * 파일을 GCS에 업로드하고, 업로드 성공 시 인코딩된 파일명만 반환합니다.
     *
     * @param file 업로드할 파일
     * @return "파일 업로드 성공: {인코딩된 파일명}" 또는 에러 메시지
     */
    public String uploadFile(MultipartFile file) {
        try {
            log.info("파일 업로드 시작 - 원본 파일명: {}", file.getOriginalFilename());
            // UUID를 파일명 앞에 붙여 고유 파일명 생성
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            log.debug("생성된 고유 파일명: {}", fileName);

            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            Blob blob = storage.create(blobInfo, file.getBytes());
            log.info("버킷 '{}'에 파일 업로드 완료, 파일명: {}", bucketName, fileName);

            // 파일명을 URL 인코딩 (공백은 %20으로 인코딩)
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replace("+", "%20");
            // DB에 저장할 때는 파일명만 저장
            return "파일 업로드 성공: " + encodedFileName;
        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", e.getMessage());
            return "파일 업로드 실패: " + e.getMessage();
        }
    }

    /**
     * 지정된 fileName(인코딩된 가능)을 디코딩하여 GCS 객체를 삭제.
     */
    public boolean deleteFile(String fileName) {
        try {
            // DB에 인코딩된 형태로 저장된 파일명을 디코딩
            String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
            // 예: "0831bfc8-c9c8-452c-b8e3-854307f5a1bc_%EC%8A%A4..."
            //  -> "0831bfc8-c9c8-452c-b8e3-854307f5a1bc_스크린샷 2025-01-06 095410.png"

            log.info("디코딩된 파일명: {}", decodedFileName);

            BlobId blobId = BlobId.of(bucketName, decodedFileName);
            boolean deleted = storage.delete(blobId);
            if (deleted) {
                log.info("파일 삭제 성공: {}", decodedFileName);
            } else {
                log.warn("파일 삭제 실패 (파일이 존재하지 않음?): {}", decodedFileName);
            }
            return deleted;
        } catch (Exception e) {
            log.error("파일 삭제 중 예외 발생: {}", e.getMessage());
            return false;
        }
    }
}
