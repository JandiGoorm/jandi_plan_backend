package com.jandi.plan_backend.resource.banner.service;

import com.jandi.plan_backend.resource.banner.dto.BannerListDTO;
import com.jandi.plan_backend.resource.banner.dto.BannerRespDTO;
import com.jandi.plan_backend.resource.banner.entity.Banner;
import com.jandi.plan_backend.resource.banner.repository.BannerRepository;
import com.jandi.plan_backend.image.dto.ImageRespDto;
import com.jandi.plan_backend.image.repository.ImageRepository;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.entity.Role;
import com.jandi.plan_backend.util.ValidationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BannerService {
    private final BannerRepository bannerRepository;
    private final ValidationUtil validationUtil;
    private final ImageService imageService;
    private final ImageRepository imageRepository;
    private final String urlPrefix = "https://storage.googleapis.com/plan-storage/";

    public BannerService(BannerRepository bannerRepository,
                         ValidationUtil validationUtil,
                         ImageService imageService,
                         ImageRepository imageRepository) {
        this.bannerRepository = bannerRepository;
        this.validationUtil = validationUtil;
        this.imageService = imageService;
        this.imageRepository = imageRepository;
    }

    /** 전체 배너 목록 조회 */
    @Transactional(readOnly = true)
    public List<BannerListDTO> getAllBanners() {
        return bannerRepository.findAll().stream()
                .map(banner -> {
                    // bannerId에 해당하는 image를 조회
                    String imageUrl = imageService.getImageByTarget("banner", banner.getBannerId())
                            .map(img -> urlPrefix + img.getImageUrl())
                            .orElse(null);

                    // BannerListDTO 생성 시, imageUrl 주입
                    return new BannerListDTO(banner, imageUrl);
                })
                .collect(Collectors.toList());
    }

    /** 배너글 작성 */
    @Transactional
    public BannerRespDTO writeBanner(String email,
                                     MultipartFile file,
                                     String title,
                                     String subtitle,
                                     String link) {
        // 1) 유저 검증 (관리자 권한 여부는 ValidationUtil 등에서 처리)
        User user = validationUtil.validateUserExists(email);

        // 2) 배너글 생성
        Banner banner = new Banner();
        banner.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        banner.setTitle(title);
        banner.setSubtitle(subtitle);
        banner.setLinkUrl(link);
        bannerRepository.save(banner);

        // 3) 이미지 업로드 (targetType = "banner", targetId = bannerId)
        ImageRespDto imageResp = imageService.uploadImage(file, user.getEmail(), banner.getBannerId(), "banner");

        // 4) 배너 응답 DTO 생성
        return new BannerRespDTO(banner, imageResp.getImageUrl());
    }

    /** 배너글 수정 */
    @Transactional
    public BannerRespDTO updateBanner(String email,
                                      Integer bannerId,
                                      MultipartFile file,
                                      String title,
                                      String subtitle,
                                      String link) {
        // 1) 유저 검증
        User user = validationUtil.validateUserExists(email);

        // 2) 배너글 검증
        Banner banner = validationUtil.validateBannerExists(bannerId);

        // 3) 값이 있으면 수정
        if (title != null && !Objects.equals(title, "")) {
            banner.setTitle(title);
        }
        if (subtitle != null && !Objects.equals(subtitle, "")) {
            banner.setSubtitle(subtitle);
        }
        if (link != null && !Objects.equals(link, "")) {
            banner.setLinkUrl(link);
        }
        bannerRepository.save(banner);

        // 4) 새 파일이 있으면 기존 이미지 삭제 후 재업로드
        if (file != null && !file.isEmpty()) {
            imageRepository.findByTargetTypeAndTargetId("banner", bannerId)
                    .ifPresent(img -> imageService.deleteImage(img.getImageId()));
            imageService.uploadImage(file, user.getEmail(), bannerId, "banner");
        }

        // 5) 최종적으로, 현재 배너의 이미지 URL 조회
        String finalImageUrl = imageService.getImageByTarget("banner", bannerId)
                .map(img -> urlPrefix + img.getImageUrl())
                .orElse(null);

        return new BannerRespDTO(banner, finalImageUrl);
    }

    /**
     * 배너글 삭제
     * 서비스 계층에서 사용자 이메일을 기반으로 관리자 권한을 검증한 후 삭제를 진행합니다.
     */
    @Transactional
    public boolean deleteBanner(String userEmail, Integer bannerId) {
        // 사용자 검증
        User user = validationUtil.validateUserExists(userEmail);
        // 관리자 권한 검증
        if (user.getRoleEnum() != Role.ADMIN) {
            throw new RuntimeException("권한이 없습니다.");
        }
        // 배너 검증 및 삭제
        Banner banner = validationUtil.validateBannerExists(bannerId);
        imageRepository.findByTargetTypeAndTargetId("banner", bannerId)
                .ifPresent(img -> imageService.deleteImage(img.getImageId()));
        bannerRepository.delete(banner);
        return true;
    }

}
