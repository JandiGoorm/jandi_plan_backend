package com.jandi.plan_backend.storage.service;

import com.jandi.plan_backend.storage.dto.ImageResponseDto;
import com.jandi.plan_backend.storage.entity.Image;
import com.jandi.plan_backend.storage.repository.ImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 이미지 업로드와 관련된 비즈니스 로직을 담당하는 서비스.
 */
@Slf4j
@Service
public class ImageService {

    private final GoogleCloudStorageService googleCloudStorageService;
    private final ImageRepository imageRepository;

    // 공개 URL 접두어 (버킷명에 맞게 수정 가능)
    private final String publicUrlPrefix = "https://storage.googleapis.com/plan-storage/";

    public ImageService(GoogleCloudStorageService googleCloudStorageService, ImageRepository imageRepository) {
        this.googleCloudStorageService = googleCloudStorageService;
        this.imageRepository = imageRepository;
    }

    /**
     * 파일을 업로드한 후, 이미지 정보를 DB에 저장합니다.
     *
     * @param file 업로드할 이미지 파일
     * @param owner JWT 토큰에서 추출한 사용자 이메일 (업로더)
     * @param targetId 대상 엔티티의 식별자
     * @param targetType 이미지가 속하는 대상
     * @return 업로드 및 DB 저장 결과를 담은 ImageResponseDto
     */
    public ImageResponseDto uploadImage(MultipartFile file, String owner, Integer targetId, String targetType) {
        String uploadResult = googleCloudStorageService.uploadFile(file);
        if (!uploadResult.startsWith("파일 업로드 성공: ")) {
            ImageResponseDto errorDto = new ImageResponseDto();
            errorDto.setMessage(uploadResult);
            return errorDto;
        }
        String storedFileName = uploadResult.replace("파일 업로드 성공: ", "").trim();

        Image image = new Image();
        image.setTargetType(targetType);
        image.setTargetId(targetId);
        image.setImageUrl(storedFileName);
        image.setOwner(owner);
        image.setCreatedAt(LocalDateTime.now());

        image = imageRepository.save(image);

        String fullPublicUrl = publicUrlPrefix + image.getImageUrl();

        ImageResponseDto responseDto = new ImageResponseDto();
        responseDto.setImageId(image.getImageId());
        responseDto.setImageUrl(fullPublicUrl);
        responseDto.setMessage("이미지 업로드 및 DB 저장 성공");
        return responseDto;
    }

    /**
     * 이미지 ID에 해당하는 이미지의 공개 URL을 반환합니다.
     *
     * @param imageId 조회할 이미지의 ID
     * @return 전체 공개 URL (예: "https://storage.googleapis.com/plan-storage/{파일명}"),
     *         이미지가 없는 경우 null 반환
     */
    public String getPublicUrlByImageId(Integer imageId) {
        Optional<Image> imageOptional = imageRepository.findById(imageId);
        return imageOptional.map(img -> publicUrlPrefix + img.getImageUrl())
                .orElse(null);
    }

    /**
     * 이미지 삭제 기능: 실제 클라우드 스토리지 버킷에서 파일을 삭제하고, DB의 해당 이미지 레코드도 삭제합니다.
     *
     * @param imageId 삭제할 이미지의 DB ID
     * @return 삭제 성공 시 true, 이미지가 없거나 삭제 실패 시 false
     */
    public boolean deleteImage(Integer imageId) {
        Optional<Image> imageOptional = imageRepository.findById(imageId);
        if (imageOptional.isEmpty()) {
            log.warn("삭제할 이미지가 DB에 존재하지 않습니다. 이미지 ID: {}", imageId);
            return false;
        }
        Image image = imageOptional.get();
        // 클라우드 스토리지에서 파일 삭제 시도
        boolean storageDeleted = googleCloudStorageService.deleteFile(image.getImageUrl());
        if (storageDeleted) {
            // 클라우드에서 삭제 성공하면 DB에서도 이미지 레코드를 삭제
            imageRepository.delete(image);
            log.info("이미지 삭제 완료: 이미지 ID: {}", imageId);
            return true;
        } else {
            log.warn("클라우드 스토리지에서 이미지 삭제 실패: 이미지 ID: {}", imageId);
            return false;
        }
    }
}
