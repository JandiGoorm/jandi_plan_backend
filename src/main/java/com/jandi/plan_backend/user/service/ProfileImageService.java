package com.jandi.plan_backend.user.service;

import com.jandi.plan_backend.storage.dto.ImageResponseDto;
import com.jandi.plan_backend.storage.entity.Image;
import com.jandi.plan_backend.storage.repository.ImageRepository;
import com.jandi.plan_backend.storage.service.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * 프로필 사진 업로드, 조회, 삭제, 업데이트와 관련된 비즈니스 로직을 담당하는 서비스.
 */
@Service
public class ProfileImageService {

    private final ImageService imageService;
    private final ImageRepository imageRepository;

    public ProfileImageService(ImageService imageService, ImageRepository imageRepository) {
        this.imageService = imageService;
        this.imageRepository = imageRepository;
    }

    /**
     * 프로필 사진 업로드.
     *
     * @param file       업로드할 이미지 파일
     * @param ownerEmail 업로더 이메일
     * @param userId     인증된 사용자의 ID (targetId)
     * @return 업로드 결과를 담은 ImageResponseDto
     */
    public ImageResponseDto uploadProfileImage(MultipartFile file, String ownerEmail, Integer userId) {
        String targetType = "userProfile";
        return imageService.uploadImage(file, ownerEmail, userId, targetType);
    }

    /**
     * 프로필 사진 조회.
     *
     * @param userId 인증된 사용자의 ID
     * @return 해당 프로필 사진 Image 엔티티 (Optional)
     */
    public Optional<Image> getProfileImage(Integer userId) {
        return imageRepository.findByTargetTypeAndTargetId("userProfile", userId);
    }

    /**
     * 프로필 사진 삭제.
     *
     * @param userId 인증된 사용자의 ID
     * @return 삭제 성공 여부 (true: 삭제됨, false: 이미지 없음)
     */
    public boolean deleteProfileImage(Integer userId) {
        Optional<Image> imageOpt = getProfileImage(userId);
        if (imageOpt.isPresent()) {
            Image image = imageOpt.get();
            imageRepository.delete(image);
            return true;
        }
        return false;
    }

    /**
     * 프로필 사진 업데이트.
     * 기존의 프로필 사진을 삭제한 후, 새 이미지를 업로드합니다.
     *
     * @param file       새로 업로드할 이미지 파일
     * @param ownerEmail 업로더 이메일
     * @param userId     인증된 사용자의 ID
     * @return 업데이트 결과를 담은 ImageResponseDto
     */
    public ImageResponseDto updateProfileImage(MultipartFile file, String ownerEmail, Integer userId) {
        deleteProfileImage(userId);
        return uploadProfileImage(file, ownerEmail, userId);
    }
}
