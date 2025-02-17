package com.jandi.plan_backend.user.service;

import com.jandi.plan_backend.storage.dto.ImageResponseDto;
import com.jandi.plan_backend.storage.service.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 프로필 사진 업로드 관련 비즈니스 로직을 담당하는 서비스.
 * 이 서비스는 공통 이미지 업로드 로직을 제공하는 storage.ImageService를 활용하여
 * 프로필 사진 업로드에 필요한 targetType("profile") 및 targetId(인증된 사용자 ID)를 설정합니다.
 */
@Service
public class ProfileImageService {

    private final ImageService imageService;

    public ProfileImageService(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * 프로필 사진을 업로드합니다.
     *
     * @param file 업로드할 이미지 파일
     * @param ownerEmail 업로더의 이메일 (인증된 사용자)
     * @param userId 인증된 사용자의 userId (targetId)
     * @return 업로드 결과를 담은 ImageResponseDto
     */
    public ImageResponseDto uploadProfileImage(MultipartFile file, String ownerEmail, Integer userId) {
        // 프로필 사진은 targetType이 "profile"로 고정됨
        String targetType = "profile";
        return imageService.uploadImage(file, ownerEmail, userId, targetType);
    }
}
