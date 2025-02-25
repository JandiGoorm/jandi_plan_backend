package com.jandi.plan_backend.resource.service;

import com.jandi.plan_backend.image.entity.Image;
import com.jandi.plan_backend.resource.dto.BannerListDTO;
import com.jandi.plan_backend.resource.dto.BannerRespDTO;
import com.jandi.plan_backend.resource.entity.Banner;
import com.jandi.plan_backend.resource.repository.BannerRepository;
import com.jandi.plan_backend.image.dto.ImageRespDto;
import com.jandi.plan_backend.image.repository.ImageRepository;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BannerService {
    private final BannerRepository bannerRepository;
    private final ValidationUtil validationUtil;
    private final ImageService imageService;
    private final ImageRepository imageRepository;


    //생성자를 통한 의존성 주입
    public BannerService(BannerRepository bannerRepository, ValidationUtil validationUtil, ImageService imageService, ImageRepository imageRepository) {
        this.bannerRepository = bannerRepository;
        this.validationUtil = validationUtil;
        this.imageService = imageService;
        this.imageRepository = imageRepository;
    }

    /** 전체 배너 목록 조회*/
    public List<BannerListDTO> getAllBanners() {
        return bannerRepository.findAll().stream()
                .map(BannerListDTO::new)
                .collect(Collectors.toList());
    }

    /** 배너글 작성 */
    public BannerRespDTO writeBanner(
            String email, MultipartFile file, String title, String link) {
        //유저 검증
        User user = validationUtil.validateUserExists(email);
        validationUtil.validateUserIsAdmin(user);

        //배너글 생성
        Banner banner = new Banner();
        banner.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        banner.setTitle(title);
        banner.setLinkUrl(link);
        bannerRepository.save(banner); //imageUrl 미포함된 상태로 1차 저장

        //이미지 업로드
        ImageRespDto image = imageService.uploadImage(
                file, user.getEmail(), banner.getBannerId(), "banner");
        banner.setImageUrl(image.getImageUrl()); //업로드된 imageUrl 저장

        //DB 최종 저장 및 반환
        bannerRepository.save(banner); //imageUrl 포함하여 저장
        return new BannerRespDTO(banner);
    }

    /** 배너글 수정 */
    public BannerRespDTO updateBanner(
            String email, Integer bannerId, MultipartFile file, String title, String link) {
        //유저 검증
        User user = validationUtil.validateUserExists(email);
        validationUtil.validateUserIsAdmin(user);

        //배너글 검증
        Banner banner = validationUtil.validateBannerExists(bannerId);

        //배너 수정: 값이 있는 것만 수정
        if (title != null) { banner.setTitle(title); }
        if (link != null) { banner.setLinkUrl(link); }
        if (file != null) {
            imageRepository.findByTargetTypeAndTargetId("banner", bannerId)
                    .ifPresent(imageRepository::delete); //기존 이미지 삭제
            ImageRespDto image = imageService.uploadImage(
                    file, user.getEmail(), bannerId, "banner"); //재업로드
            banner.setImageUrl(image.getImageUrl()); //업로드된 imageUrl 저장
        }

        //수정된 배너 반환
        bannerRepository.save(banner);
        return new BannerRespDTO(banner);
    }

    public boolean deleteBanner(String userEmail, Integer bannerId) {
        //유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(user);

        //배너글 검증
        Banner banner = validationUtil.validateBannerExists(bannerId);

        //배너 삭제 및 반환
        imageRepository.findByTargetTypeAndTargetId("banner", bannerId)
                .ifPresent(imageRepository::delete); //기존 이미지 삭제
        bannerRepository.delete(banner);
        return true;
    }
}
