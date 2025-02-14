package com.jandi.plan_backend.storage;

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

/**
 * Google Cloud Storage에 파일 업로드와 다운로드를 처리하는 서비스 클래스.
 * 파일을 GCS 버킷에 업로드한 후, 업로드 성공 여부에 따라 메시지를 반환함.
 */
@Slf4j
@Service
public class GoogleCloudStorageService {

    // GCS Storage API를 사용하기 위한 Storage 객체
    private final Storage storage;

    // application.properties에 정의된 GCS 버킷 이름
    @Value("${gcs.bucket.name}")
    private String bucketName;

    // 생성자 주입
    public GoogleCloudStorageService(Storage storage) {
        this.storage = storage;
    }

    /**
     * MultipartFile 객체를 받아서 Google Cloud Storage에 업로드하고, 업로드 결과 메시지를 반환함.
     *
     * 1. 파일 이름에 UUID를 붙여서 고유 파일 이름 생성.
     * 2. BlobId와 BlobInfo를 생성해 파일의 메타데이터(콘텐츠 타입)를 설정.
     * 3. Storage.create()를 사용해서 파일을 업로드.
     * 4. 업로드 성공 시 "파일 업로드 성공: {파일명}" 메시지, 실패 시 "파일 업로드 실패: {에러 메시지}"를 반환.
     *
     * @param file 업로드할 파일 (MultipartFile)
     * @return 업로드 결과 메시지
     */
    public String uploadFile(MultipartFile file) {
        try {
            log.info("파일 업로드 시작 - 원본 파일명: {}", file.getOriginalFilename());

            // UUID를 파일 이름 앞에 붙여 중복 없이 고유한 파일명 생성
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            log.debug("생성된 고유 파일명: {}", fileName);

            // GCS 버킷 내에서 파일 위치를 지정하기 위한 BlobId 생성
            BlobId blobId = BlobId.of(bucketName, fileName);

            // BlobInfo 생성 (파일의 콘텐츠 타입 설정)
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            // 파일 내용을 바이트 배열로 읽어 Blob 생성 (업로드 수행)
            Blob blob = storage.create(blobInfo, file.getBytes());
            log.info("버킷 '{}'에 파일 업로드 완료, 파일명: {}", bucketName, fileName);

            return "파일 업로드 성공: " + fileName;
        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", e.getMessage());
            return "파일 업로드 실패: " + e.getMessage();
        }
    }

    /**
     * 지정된 파일명을 사용해 Google Cloud Storage에서 파일을 다운로드하고,
     * 파일 내용을 바이트 배열로 반환함.
     *
     * 1. BlobId를 사용해 GCS 버킷에서 해당 파일 조회.
     * 2. 파일이 존재하지 않으면 null 반환.
     * 3. 파일이 존재하면 Blob.getContent()로 데이터를 추출.
     *
     * @param fileName 다운로드할 파일명
     * @return 파일 내용을 담은 바이트 배열, 파일이 없으면 null
     */
    public byte[] downloadFile(String fileName) {
        log.info("파일 다운로드 시도 - 파일명: {}", fileName);
        Blob blob = storage.get(BlobId.of(bucketName, fileName));
        if (blob == null) {
            log.warn("파일을 찾지 못함 - 파일명: {}", fileName);
            return null;
        }
        log.info("파일 다운로드 성공 - 파일명: {}", fileName);
        return blob.getContent();
    }
}
