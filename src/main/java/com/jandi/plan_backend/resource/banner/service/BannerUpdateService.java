package com.jandi.plan_backend.resource.banner.service;

import com.jandi.plan_backend.resource.banner.dto.BannerReqDTO;
import com.jandi.plan_backend.resource.banner.dto.BannerRespDTO;
import com.jandi.plan_backend.resource.banner.entity.Banner;
import com.jandi.plan_backend.resource.banner.repository.BannerRepository;
import com.jandi.plan_backend.image.dto.ImageRespDto;
import com.jandi.plan_backend.image.repository.ImageRepository;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.entity.Role;
import com.jandi.plan_backend.util.BannerUtil;
import com.jandi.plan_backend.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class BannerUpdateService {
    private final ValidationUtil validationUtil;
    private final BannerUtil bannerUtil;
    private final BannerRepository bannerRepository;

    /** 배너글 작성 */
    @Transactional
    public BannerRespDTO writeBanner(String email, BannerReqDTO reqDTO) {
        // 1) 유저 검증
        User user = validationUtil.validateUserExists(email);

        // 2) 배너 생성
        Banner banner = createBannerData(reqDTO);
        bannerUtil.uploadBannerImage(banner, user, reqDTO.getFile());

        // 3) 배너 응답 DTO 생성
        String bannerImage = bannerUtil.getBannerImage(banner);
        return new BannerRespDTO(banner, bannerImage);
    }

    /** 배너글 수정 */
    @Transactional
    public BannerRespDTO updateBanner(String email, Integer bannerId, BannerReqDTO reqDTO) {
        // 1) 유저 검증
        User user = validationUtil.validateUserExists(email);

        // 2) 배너 검증 및 수정
        Banner banner = validationUtil.validateBannerExists(bannerId);
        updateBannerData(banner, user, reqDTO);

        // 3) 배너 응답 DTO 생성
        String bannerImage = bannerUtil.getBannerImage(banner);
        return new BannerRespDTO(banner, bannerImage);
    }

    /**
     * 배너글 삭제
     * 스프링 시큐리티가 사용자 역할을 기반으로 권한을 검증하므로 서비스 계층에서는 삭제만 담당합니다.
     */
    @Transactional
    public boolean deleteBanner(String userEmail, Integer bannerId) {
        // 배너 검증 및 삭제
        Banner banner = validationUtil.validateBannerExists(bannerId);
        deleteBannerData(banner);

        return !bannerRepository.existsById(Long.valueOf(bannerId));
    }

    private Banner createBannerData(BannerReqDTO reqDTO) {
        Banner banner = new Banner();
        banner.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        banner.setTitle(reqDTO.getTitle());
        banner.setSubtitle(reqDTO.getSubTitle());
        banner.setLinkUrl(reqDTO.getLinkUrl());
        bannerRepository.save(banner);

        return banner;
    }

    private void updateBannerData(Banner banner, User user, BannerReqDTO reqDTO) {
        if (validatePresent(reqDTO.getTitle())) { // 제목 수정
            banner.setTitle(reqDTO.getTitle());
        }
        if (validatePresent(reqDTO.getSubTitle())) { // 소제목 수정
            banner.setSubtitle(reqDTO.getSubTitle());
        }
        if (validatePresent(reqDTO.getLinkUrl())) { // 링크 수정
            banner.setLinkUrl(reqDTO.getLinkUrl());
        }

        // 새 파일이 있으면 기존 이미지 삭제 후 재업로드
        if (validatePresent(reqDTO.getFile())) {
            bannerUtil.deleteBannerImage(banner);
            bannerUtil.uploadBannerImage(banner, user, reqDTO.getFile());
        }
    }

    private void deleteBannerData(Banner banner){
        bannerUtil.deleteBannerImage(banner);
        bannerRepository.delete(banner);
    }

    // 텍스트 존재 여부 확인
    private boolean validatePresent(String string) {
        return string != null && !Objects.equals(string, "");
    }

    // 파일 존재 여부 확인
    private boolean validatePresent(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

}
