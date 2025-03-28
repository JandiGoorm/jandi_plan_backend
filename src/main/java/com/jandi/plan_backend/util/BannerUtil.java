package com.jandi.plan_backend.util;

import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.image.dto.ImageRespDto;
import com.jandi.plan_backend.image.entity.Image;
import com.jandi.plan_backend.image.repository.ImageRepository;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.resource.banner.entity.Banner;
import com.jandi.plan_backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BannerUtil {
    private final ImageService imageService;
    private final ImageRepository imageRepository;
    @Value("${image-prefix}") private String prefix;

    // 배너 이미지 삭제
    public void deleteBannerImage(Banner banner){
        imageRepository.findByTargetTypeAndTargetId("banner", banner.getBannerId())
                .ifPresent(img -> imageService.deleteImage(img.getImageId()));
    }

    // 배너 이미지 업로드
    public void uploadBannerImage(Banner banner, User user, MultipartFile file) {
        imageService.uploadImage(
                file, user.getEmail(), banner.getBannerId(), "banner");
    }

    // 배너 이미지 조회
    public String getBannerImage(Banner banner) {
        Image image = imageRepository.findByTargetTypeAndTargetId("banner", banner.getBannerId())
                .orElse(null);

        return (image == null) ? "" : prefix + image.getImageUrl();
    }
}
