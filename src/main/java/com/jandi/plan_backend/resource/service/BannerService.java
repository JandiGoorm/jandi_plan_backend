package com.jandi.plan_backend.resource.service;

import com.jandi.plan_backend.resource.dto.BannerListDTO;
import com.jandi.plan_backend.resource.dto.BannerRespDTO;
import com.jandi.plan_backend.resource.entity.Banner;
import com.jandi.plan_backend.resource.repository.BannerRepository;
import com.jandi.plan_backend.storage.dto.ImageResponseDto;
import com.jandi.plan_backend.storage.service.ImageService;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BannerService {
    private final BannerRepository bannerRepository;
    private final ValidationUtil validationUtil;
    private final ImageService imageService;


    //생성자를 통한 의존성 주입
    public BannerService(BannerRepository bannerRepository, ValidationUtil validationUtil, ImageService imageService) {
        this.bannerRepository = bannerRepository;
        this.validationUtil = validationUtil;
        this.imageService = imageService;
    }

    /** 전체 배너 목록 조회*/
    public List<BannerListDTO> getAllBanners() {
        return bannerRepository.findAll().stream()
                .map(BannerListDTO::new)
                .collect(Collectors.toList());
    }

    /** 배너글 작성 */
    public BannerRespDTO writeBanner(String email, MultipartFile file, String title, String link) {
        //유저 검증
        User user = validationUtil.validateUserExists(email);
        validationUtil.validateUserIsAdmin(user);

        //배너글 생성
        Banner banner = new Banner();
        banner.setCreatedAt(LocalDateTime.now());
        banner.setTitle(title);
        banner.setLinkUrl(link);
        bannerRepository.save(banner); //imageUrl 미포함된 상태로 1차 저장

        //이미지 업로드
        ImageResponseDto image = imageService.uploadImage(
                file, user.getEmail(), banner.getBannerId(), "banner");
        banner.setImageUrl(image.getImageUrl()); //업로드된 imageUrl 저장

        //DB 최종 저장 및 반환
        bannerRepository.save(banner); //imageUrl 포함하여 저장
        return new BannerRespDTO(banner);
    }
}
