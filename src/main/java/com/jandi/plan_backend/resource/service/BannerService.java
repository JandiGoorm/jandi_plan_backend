package com.jandi.plan_backend.resource.service;

import com.jandi.plan_backend.resource.dto.BannerListDTO;
import com.jandi.plan_backend.resource.dto.BannerReqDTO;
import com.jandi.plan_backend.resource.dto.BannerRespDTO;
import com.jandi.plan_backend.resource.entity.Banner;
import com.jandi.plan_backend.resource.repository.BannerRepository;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BannerService {
    private final BannerRepository bannerRepository;
    private final ValidationUtil validationUtil;


    //생성자를 통한 의존성 주입
    public BannerService(BannerRepository bannerRepository, ValidationUtil validationUtil) {
        this.bannerRepository = bannerRepository;
        this.validationUtil = validationUtil;
    }

    /** 전체 배너 목록 조회*/
    public List<BannerListDTO> getAllBanners() {
        //return bannerRepository.findAll();
        return bannerRepository.findAll().stream()
                .map(BannerListDTO::new)
                .collect(Collectors.toList());
    }

    /** 배너글 작성 */
    public BannerRespDTO writeBanner(BannerReqDTO bannerDTO, String email) {
        //유저 검증
        User user = validationUtil.validateUserExists(email);
        validationUtil.validateUserIsAdmin(user);

        //배너글 생성
        Banner banner = new Banner();
        banner.setCreatedAt(LocalDateTime.now());
        banner.setTitle(bannerDTO.getTitle());
        banner.setImageUrl(bannerDTO.getImageUrl());
        banner.setLinkUrl(bannerDTO.getLinkUrl());

        //DB 저장 및 반환
        bannerRepository.save(banner);
        return new BannerRespDTO(banner);
    }
}
