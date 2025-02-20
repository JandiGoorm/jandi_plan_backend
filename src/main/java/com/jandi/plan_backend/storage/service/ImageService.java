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
        // Google Cloud Storage에 파일 업로드 및 인코딩된 파일명 반환
        String uploadResult = googleCloudStorageService.uploadFile(file);
        if (!uploadResult.startsWith("파일 업로드 성공: ")) {
            ImageResponseDto errorDto = new ImageResponseDto();
            errorDto.setMessage(uploadResult);
            return errorDto;
        }
        // 업로드 성공: 인코딩된 파일명 추출 (예: "c85ea963-8d42-4f2e-83c7-e0087bead6f4_unnamed.png")
        String storedFileName = uploadResult.replace("파일 업로드 성공: ", "").trim();

        // Image 엔티티 생성 및 DB 저장 (파일명만 저장)
        Image image = new Image();
        image.setTargetType(targetType);
        image.setTargetId(targetId);
        image.setImageUrl(storedFileName);
        image.setOwner(owner);
        image.setCreatedAt(LocalDateTime.now());

        image = imageRepository.save(image);

        // 전체 공개 URL 구성 (접두어 + 파일명)
        String fullPublicUrl = publicUrlPrefix + image.getImageUrl();

        // 응답 DTO 생성
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
     * @return "https://storage.googleapis.com/plan-storage/{파일명}" 형태의 공개 URL, 이미지가 없는 경우 null 반환
     */
    public String getPublicUrlByImageId(Integer imageId) {
        Optional<Image> imageOptional = imageRepository.findById(imageId);
        return imageOptional.map(img -> publicUrlPrefix + img.getImageUrl())
                .orElse(null);
    }

    public String getUserProfile(Integer userId) {
        Optional<Image> imageOptional = imageRepository.findByTargetTypeAndTargetId("userProfile", userId);
        return imageOptional.map(img -> publicUrlPrefix + img.getImageUrl()).orElse(null);
    }
}
