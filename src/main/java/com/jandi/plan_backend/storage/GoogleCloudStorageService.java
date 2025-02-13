package com.jandi.plan_backend.storage;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class GoogleCloudStorageService {

    private final Storage storage;

    @Value("${gcs.bucket.name}")
    private String bucketName;

    public GoogleCloudStorageService(Storage storage) {
        this.storage = storage;
    }

    /**
     * 이미지 업로드 메서드
     * 파일을 Google Cloud Storage에 업로드하고, public 읽기 권한을 부여한 후 URL을 반환합니다.
     */
    public String uploadFile(MultipartFile file) throws IOException {
        log.info("Starting image upload for file: {}", file.getOriginalFilename());
        // 파일명에 UUID를 붙여 유니크하게 생성
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        log.debug("Generated unique file name: {}", fileName);

        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        // 파일 업로드
        Blob blob = storage.create(blobInfo, file.getBytes());
        log.info("File uploaded to bucket '{}' with file name: {}", bucketName, fileName);

        // 업로드된 파일에 public 읽기 권한 부여
        storage.createAcl(blobId, Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
        log.info("Public read access granted for file: {}", fileName);

        String publicUrl = String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
        log.info("Returning public URL: {}", publicUrl);
        return publicUrl;
    }

    /**
     * 이미지 다운로드 메서드
     * 저장된 파일의 이름을 받아 파일 내용을 바이트 배열로 반환합니다.
     */
    public byte[] downloadFile(String fileName) {
        log.info("Attempting to download file: {}", fileName);
        Blob blob = storage.get(BlobId.of(bucketName, fileName));
        if (blob == null) {
            log.warn("File not found: {}", fileName);
            return null;
        }
        log.info("File downloaded successfully: {}", fileName);
        return blob.getContent();
    }
}
